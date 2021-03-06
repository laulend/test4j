package org.test4j.module.dbfit.environment;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.test4j.module.database.environment.DBEnvironment;
import org.test4j.module.database.environment.TableMeta;
import org.test4j.module.database.environment.types.DB2Environment;
import org.test4j.module.database.environment.types.DerbyEnvironment;
import org.test4j.module.database.environment.types.MySqlEnvironment;
import org.test4j.module.database.environment.types.OracleEnvironment;
import org.test4j.module.database.environment.types.SqlServerEnvironment;
import org.test4j.module.database.transaction.TransactionManagementConfiguration;
import org.test4j.module.dbfit.model.BigDecimalParseDelegate;
import org.test4j.module.dbfit.model.DbParameterAccessor;
import org.test4j.module.dbfit.model.SqlDateParseDelegate;
import org.test4j.module.dbfit.model.SqlTimestampParseDelegate;
import org.test4j.module.dbfit.utility.SymbolUtil;

import fit.TypeAdapter;

public abstract class DbFitAbstractDBEnvironment implements DbFitEnvironment {
	protected DBEnvironment dbEnviroment;

	protected DbFitAbstractDBEnvironment(DBEnvironment dbEnvironemnt) {
		this.dbEnviroment = dbEnvironemnt;
		TypeAdapter.registerParseDelegate(BigDecimal.class, BigDecimalParseDelegate.class);
		TypeAdapter.registerParseDelegate(java.sql.Date.class, SqlDateParseDelegate.class);
		TypeAdapter.registerParseDelegate(java.sql.Timestamp.class, SqlTimestampParseDelegate.class);
	}

	public final PreparedStatement createStatementWithBoundFixtureSymbols(String commandText) throws SQLException {
		String text = SymbolUtil.replacedBySymbols(commandText);
		String paramNames[] = extractParamNames(text);

		String sql = parseCommandText(text, paramNames);

		Connection connection = this.connect();
		PreparedStatement cs = connection.prepareStatement(sql);
		for (int i = 0; i < paramNames.length; i++) {
			Object value = org.test4j.module.dbfit.utility.SymbolUtil.getSymbol(paramNames[i]);
			cs.setObject(i + 1, value);
		}
		return cs;
	}

	protected String parseCommandText(String commandText, String[] vars) {
		return commandText;
	}

	/**
	 * MUST RETURN PARAMETER NAMES IN EXACT ORDER AS IN STATEMENT. IF SINGLE
	 * PARAMETER APPEARS MULTIPLE TIMES, MUST BE LISTED MULTIPLE TIMES IN THE
	 * ARRAY ALSO
	 */
	public String[] extractParamNames(String commandText) {
		ArrayList<String> hs = new ArrayList<String>();
		Matcher mc = getParameterPattern().matcher(commandText);
		while (mc.find()) {
			String var = mc.group(1);
			if (SymbolUtil.hasSymbol(var)) {
				hs.add(var);
			}

		}
		String[] array = new String[hs.size()];
		return hs.toArray(array);
	}

	/**
	 * by default, this will support retrieving a single autogenerated key via
	 * JDBC. DB environments which support automated column retrieval after
	 * insert, like oracle, should override this and put in parameters for OUT
	 * accessors
	 */
	public String buildInsertCommand(String tableName, DbParameterAccessor[] accessors) {
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(tableName).append("(");
		String comma = "";

		StringBuilder values = new StringBuilder();

		for (DbParameterAccessor accessor : accessors) {
			if (accessor.getDirection() == DbParameterAccessor.INPUT) {
				sb.append(comma);
				values.append(comma);
				sb.append(this.getFieldQuato()).append(accessor.getName()).append(this.getFieldQuato());
				// values.append(":").append(accessor.getName());
				values.append("?");
				comma = ",";
			}
		}
		sb.append(") values (");
		sb.append(values);
		sb.append(")");
		return sb.toString();
	}

	public String buildDeleteCommand(String tableName, DbParameterAccessor[] accessors) {
		StringBuilder sb = new StringBuilder("delete from " + tableName + " where ");
		String comma = "";
		for (DbParameterAccessor accessor : accessors) {
			if (accessor.getDirection() == DbParameterAccessor.INPUT) {
				sb.append(comma);
				sb.append(accessor.getName());
				sb.append("=?");
				comma = ",";
			}
		}
		return sb.toString();
	}

	public final void teardown() throws SQLException {
	}

	// ===========================================
	public void setDataSource(String driver, String url, String schemas, String username, String password) {
		this.dbEnviroment.setDataSource(driver, url, schemas, username, password);
	}

	/**
	 * 连接当前数据源
	 * 
	 * @return
	 */
	public Connection connect() {
		return this.dbEnviroment.connect();
	}

	public void commit() {
		this.dbEnviroment.commit();
	}

	public void rollback() {
		this.dbEnviroment.rollback();
	}

	public int getExceptionCode(SQLException dbException) {
		return this.dbEnviroment.getExceptionCode(dbException);
	}

	protected abstract Pattern getParameterPattern();

	/**
	 * by default, this is set to false.
	 * 
	 * @see org.test4j.module.database.environment.DBEnvironment#supportsOuputOnInsert()
	 */
	public boolean supportsOuputOnInsert() {
		return false;
	}

	/**
	 * 连接数据源，如果先前没有建立过连接的话
	 * 
	 * @param environment
	 * @return 返回数据源连接
	 * @throws SQLException
	 */
	public Connection connectIfNeeded() {
		return this.dbEnviroment.connect();
	}

	/**
	 * 获得数据表的元信息
	 * 
	 * @param table
	 * @return
	 * @throws Exception
	 */
	public TableMeta getTableMetaData(String table) {
		return this.dbEnviroment.getTableMetaData(table);
	}

	public Object getDefaultValue(String javaType) {
		return this.dbEnviroment.getDefaultValue(javaType);
	}

	public Object toObjectValue(String input, String javaType) {
		return this.dbEnviroment.toObjectValue(input, javaType);
	}

	/**
	 * {@inheritDoc} <br>
	 * <br>
	 * 默认不做转换
	 */
	public Object converToSqlValue(Object value) {
		return value;
	}

	/** Check the validity of the supplied connection. */
	public static void checkConnectionValid(final Connection conn) throws SQLException {
		if (conn == null || conn.isClosed()) {
			throw new IllegalArgumentException("No open connection to a database is available. "
					+ "Make sure your database is running and that you have connected before performing any queries.");
		}
	}

	public static DbFitEnvironment convert(DBEnvironment environment) {
		DbFitEnvironment dbfit = null;
		if (environment instanceof DB2Environment) {
			dbfit = new DbFitDB2Environment(environment);
		} else if (environment instanceof DerbyEnvironment) {
			dbfit = new DbFitDerbyEnvironment(environment);
		} else if (environment instanceof MySqlEnvironment) {
			dbfit = new DbFitMySqlEnvironment(environment);
		} else if (environment instanceof OracleEnvironment) {
			dbfit = new DbFitOracleEnvironment(environment);
		} else if (environment instanceof SqlServerEnvironment) {
			dbfit = new DbFitSqlServerEnvironment(environment);
		}
		if (dbfit == null) {
			throw new RuntimeException("unkown database type[" + environment.getClass().getName() + "].");
		} else {
			return dbfit;
		}
	}

	public DataSource getDataSource() {
		return this.dbEnviroment.getDataSource();
	}

	public DataSource getDataSourceAndActivateTransactionIfNeeded() {
		return this.dbEnviroment.getDataSourceAndActivateTransactionIfNeeded();
	}

	public void registerTransactionManagementConfiguration(
			TransactionManagementConfiguration transactionManagementConfiguration) {
		this.dbEnviroment.registerTransactionManagementConfiguration(transactionManagementConfiguration);

	}

	public void startTransaction() {
		this.dbEnviroment.startTransaction();
	}

	public void endTransaction() {
		this.dbEnviroment.endTransaction();
	}
}
