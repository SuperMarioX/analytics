package com.cnebula.analytics.reportserver.aas;

import com.cnebula.aas.util.PolicyRuleParseEx;
import com.cnebula.aas.util.PolicyRuleParseException;

public class PermissionRule extends com.cnebula.um.ejb.entity.perm.PermissionRule {

	private static final long serialVersionUID = -9091789502039755866L;

	@Override
	public void compile() throws PolicyRuleParseException {

		StringBuilder rt = new StringBuilder();
		if (expression != null && expression.trim().length() > 0) {
			rt.append("(").append(expression).append(")");
		}

		if (entityLimitType != null) {
			if (rt.length() > 0) {
				rt.append("&");
			}
			rt.append("(typeOf(r)=").append("\"").append(entityLimitType).append("\"").append("|");
			rt.append("r.type.name = ").append("\"").append(entityLimitType).append("\")");
			if (operations != null && operations.trim().length() > 0) {

				String opsStr = "ops <= {";
				String conStr = "";
				boolean appendEntityConstraint = false; // copts.cpxOp= null

				String[] opArray = operations.split(",");
				for (String op : opArray) {
					opsStr += ("\"" + op + "\",");
					if (!appendEntityConstraint) {
						conStr += "(copts.cpxOp = null)|";
						appendEntityConstraint = true;
					}
				}
				if (opsStr.endsWith(",")) {
					opsStr = opsStr.substring(0, opsStr.length() - 1);
				}
				opsStr += "}";
				if (conStr.endsWith("|")) {
					conStr = conStr.substring(0, conStr.length() - 1);
				}
				rt.append("&").append(opsStr).append("&(").append(conStr).append(")");
			}
		}

		if (rt.length() == 0) {
			rt.append("true");
		}
		try {
			compiledAST = PolicyRuleParseEx.parse(rt.toString());
		} catch (PolicyRuleParseException e) {
			System.out.println(rt.toString());
			e.printStackTrace();
			throw new PolicyRuleParseException(e.getMessage() + " in rule:" + name);
		}
	}

}
