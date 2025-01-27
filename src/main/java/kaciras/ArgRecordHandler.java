package kaciras;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;

/*
 * 原本是想从 mariadb-java-client 实现的 PreparedStatement 对象着手获取 SQL 的，
 * 但看了 2.x 和 3.x 巨大的变化之后感觉太不稳定了，还是外层代理更好。
 */
@RequiredArgsConstructor
final class ArgRecordHandler implements InvocationHandler {

	// 目前的 SQL 里最多只有 3 个参数。
	private final Object[] parameters = new Object[3];

	private final PreparedStatement statement;
	private final String sql;

	public String getExecutedSql() {
		return String.format(sql.replace("?", "%s"), parameters);
	}

	@SuppressWarnings("EnhancedSwitchMigration")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		var index = (Integer) args[0] - 1;

		switch (method.getName()) {
			case "setByte":
			case "setShort":
			case "setInt":
			case "setLong":
			case "setTime":
			case "setDate":
			case "setTimestamp":
			case "setFloat":
			case "setDouble":
			case "setBoolean":
				parameters[index] = args[1];
				break;
			case "setString":
				parameters[index] = "'" + args[1] + "'";
				break;
		}
		return method.invoke(statement, args);
	}
}
