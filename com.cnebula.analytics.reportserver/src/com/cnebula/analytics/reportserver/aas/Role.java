package com.cnebula.analytics.reportserver.aas;

import static javax.persistence.FetchType.EAGER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.cnebula.aas.rt.AASRuntimeContext;
import com.cnebula.common.annotations.ejb.ui.EntityUIProperty;
import com.cnebula.common.annotations.ejb.ui.EntityUIPropertyPlugin;
import com.cnebula.common.annotations.es.Property;
import com.cnebula.common.annotations.xml.XMLIgnore;
import com.cnebula.common.ejb.EntityPermission;
import com.cnebula.common.ejb.manage.EntityQuery;
import com.cnebula.common.ejb.manage.MaintainInfo;
import com.cnebula.common.ejb.manage.Maintainable;
import com.cnebula.common.i18n.I18n;
import com.cnebula.common.i18n.I18nLocale;
import com.cnebula.common.saas.ISaasable;
import com.cnebula.common.saas.ITenant;
import com.cnebula.common.security.simple.SimpleDynamicRole;
import com.cnebula.um.ejb.entity.perm.UserRule;
import com.cnebula.um.ejb.entity.usr.UMPrincipal;
import com.cnebula.um.ejb.saas.UMTenant;

public class Role extends SimpleDynamicRole implements Maintainable, Serializable, ISaasable, Cloneable {

	public final static int TYPE_BLACK = 0;
	public final static int TYPE_NORMAL = 1;

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Role other = (Role) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	String id;

	String category = "0";

	/**
	 * 类型 0 是扣除权限, 1 是正常角色
	 */
	int type = 1;

	MaintainInfo maintainInfo = new MaintainInfo();

	private int version;

	@EntityUIProperty(readOnly = true, i18n = @I18n(defaultLang = "zh_CN", items = { @I18nLocale(lang = "zh_CN", value = "版本"),
			@I18nLocale(lang = "en_US", value = "Version") }))
	@Version
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Role() {
	}

	@Transient
	public boolean isBlackRole() {
		return type == 0;
	}

	@Id
	@GeneratedValue
	@Column(name = "id")
	@EntityUIProperty(readOnly = true, i18n = @I18n(defaultLang = "zh_CN", items = { @I18nLocale(lang = "zh_CN", value = "标识"),
			@I18nLocale(lang = "en_US", value = "ID") }))
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@EntityUIProperty(i18n = @I18n(defaultLang = "zh_CN", items = { @I18nLocale(lang = "zh_CN", value = "名称"),
			@I18nLocale(lang = "en_US", value = "Name") }))
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@EntityUIProperty(i18n = @I18n(defaultLang = "zh_CN", items = { @I18nLocale(lang = "zh_CN", value = "类别"),
			@I18nLocale(lang = "en_US", value = "Type") }), plugin = @EntityUIPropertyPlugin(name = "IComboView", properties = {
			@Property(name = "dataProvider", value = "i18n://com.cnebula.um.ejb.entity.perm.Role.category"),
			@Property(name = "viewStyle", value = ((1 << 3)) + "", type = Property.Type.Integer) }))
	@Column(length = 255)
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Embedded
	public MaintainInfo getMaintainInfo() {
		return maintainInfo;
	}

	public void setMaintainInfo(MaintainInfo maintainInfo) {
		this.maintainInfo = maintainInfo;
	}

	@EntityUIProperty(readOnly = true, i18n = @I18n(defaultLang = "zh_CN", items = { @I18nLocale(lang = "zh_CN", value = "所有者"),
			@I18nLocale(lang = "en_US", value = "Owner") }))
	@ManyToOne
	public UMPrincipal getUMRoleOwner() {
		return (UMPrincipal) super.getOwner();
	}

	public void setUMRoleOwner(UMPrincipal owner) {
		super.setOwner(owner);
	}

	@EntityUIProperty(i18n = @I18n(defaultLang = "zh_CN", items = { @I18nLocale(lang = "zh_CN", value = "权限过滤规则"),
			@I18nLocale(lang = "en_US", value = "Permission Filter Rules") })
	// , plugin =
	// @EntityUIPropertyPlugin(type="com.cnebula.um.admin.ui.PermissionRulePlugin")
	)
	@ManyToMany(cascade = { CascadeType.MERGE }, fetch = FetchType.EAGER)
	@JoinTable(name = "UM_Role_PermissionRule", joinColumns = { @JoinColumn(name = "Role_ID", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "Rule_ID", referencedColumnName = "id") })
	public List<PermissionRule> getUMPermissionRules() {
		return (List<PermissionRule>) super.getPermissionRules();
	}

	public void setUMUserRules(List<UserRule> userRules) {
		super.setUserRules(userRules);
	}

	public void setUMPermissionRules(List<PermissionRule> permissionRules) {
		super.setPermissionRules(permissionRules);
	}

	@EntityUIProperty(i18n = @I18n(defaultLang = "zh_CN", items = { @I18nLocale(lang = "zh_CN", value = "用户过滤规则"),
			@I18nLocale(lang = "en_US", value = "User Filter Rules") }))
	@ManyToMany(cascade = { CascadeType.MERGE }, fetch = FetchType.EAGER)
	@JoinTable(name = "UM_Role_UserRule", joinColumns = { @JoinColumn(name = "Role_ID", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "Rule_ID", referencedColumnName = "id") })
	public List<UserRule> getUMUserRules() {
		return (List<UserRule>) super.getUserRules();
	}

	@EntityUIProperty(required = true, i18n = @I18n(defaultLang = "zh_CN", items = { @I18nLocale(lang = "zh_CN", value = "父角色"),
			@I18nLocale(lang = "en_US", value = "Parent Role") }), plugin = @EntityUIPropertyPlugin(type = "SelectParentRolePlugin"))
	@ManyToOne
	public Role getUMRoleParent() {
		return (Role) super.getParent();
	}

	public void setUMRoleParent(Role parent) {
		super.setParent(parent);
	}

	@EntityUIProperty(i18n = @I18n(defaultLang = "zh_CN", items = { @I18nLocale(lang = "zh_CN", value = "启用"),
			@I18nLocale(lang = "en_US", value = "Enable") }), plugin = @EntityUIPropertyPlugin(properties = { @Property(name = "dataProvider", value = "i18n://com.cnebula.um.entity.TrueFalse") }))
	@Override
	public boolean isEnabled() {
		return super.isEnabled();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	@EntityUIProperty(i18n = @I18n(items = { @I18nLocale(lang = "zh_CN", value = "类型"), @I18nLocale(lang = "en_US", value = "Type") }), plugin = @EntityUIPropertyPlugin(name = "IComboView", properties = {
			@Property(name = "dataProvider", value = "i18n://com.cnebula.um.ejb.entity.perm.roleType"),
			@Property(name = "viewStyle", value = ((1 << 3)) + "", type = Property.Type.Integer) }))
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	private EntityQuery toEntityQuery0(String clsName, String[] operations, boolean authable) {
		EntityQuery policyQuery = null;
		for (PermissionRule pr : getUMPermissionRules()) {
			if (pr.eval(null)) {
				return EntityQuery.NO_FILTER_QUERY;
			}
			if (pr.contain(clsName, operations, authable)) {
				EntityQuery q = pr.toEntityQuery(authable);
				if (q != null) {
					if (q == EntityQuery.NO_FILTER_QUERY) {
						return q;
					}
					policyQuery = (policyQuery == null ? q : EntityQuery.createOrQuery(policyQuery, q));
				}
			}
		}
		return policyQuery;
	}

	public EntityQuery toEntityQuery(AASRuntimeContext ctx) {
		EntityPermission ep = (EntityPermission) ctx.getPermissionDetail();
		return toEntityQuery(ctx, ep.getEntityClass().getName(), ep.getOperations());
	}

	// 当前用户已满足
	public EntityQuery toEntityQuery(AASRuntimeContext ctx, String clsName, String[] operations) {

		EntityQuery result = null;
		if (!enabled) {
			return null;
		}

		// 所有的Permission Rule
		if ((result = this.toEntityQuery0(clsName, operations, false)) == null) {
			return null;
		}

		EntityQuery policyQuery = null;
		Role up = getUMRoleParent();
		Role cur = this;
		Object assigenUser = ctx.getEnvironment().getCurrentUser();
		while (up != null) {
			AASRuntimeContext upctx = ctx.getUpCheckCopy(cur.getOwner(), assigenUser);
			if (up.fitUser(upctx)) {
				policyQuery = up.toEntityQuery0(clsName, operations, true);
				if (policyQuery != null) {
					if (result == EntityQuery.NO_FILTER_QUERY) {
						result = policyQuery;
					} else if (policyQuery == EntityQuery.NO_FILTER_QUERY) {

					} else {
						result = EntityQuery.createAndQuery(result, policyQuery);
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
			assigenUser = cur.getOwner();
			cur = up;
			up = up.getUMRoleParent();
		}
		return result;
	}

	public boolean implyEntityTypeAndOp(AASRuntimeContext ctx) {
		EntityPermission permission = (EntityPermission) ctx.getPermissionDetail();
		boolean selfOk = false;
		Class entityClass = permission.getEntityClass();
		String[] operations = permission.getOperations();
		for (PermissionRule r : getUMPermissionRules()) {
			if (r.contain(entityClass, operations, false)) {
				selfOk = true;
				break;
			}
		}
		if (selfOk) {
			// check parent
			Role up = getUMRoleParent();
			Role cur = this;
			Object assigenUser = ctx.getEnvironment().getCurrentUser();
			while (up != null) {
				AASRuntimeContext upctx = ctx.getUpCheckCopy(cur.getOwner(), assigenUser);
				if (up.fitUser(upctx)) {
					boolean upOk = false;
					for (PermissionRule r : up.getUMPermissionRules()) {
						if (r.contain(entityClass, operations, false)) {
							upOk = true;
							break;
						}
					}
					if (!upOk) {
						return false;
					}
				} else {
					return false;
				}
				assigenUser = cur.getOwner();
				cur = up;
				up = up.getUMRoleParent();
			}
			return true;
		}
		return false;
	}

	// 针对SAAS改造增加的方法

	private UMTenant tenant;

	@EntityUIProperty(i18n = @I18n(defaultLang = "zh_CN", items = { @I18nLocale(lang = "zh_CN", value = "租客"),
			@I18nLocale(lang = "en_US", value = "Calis ID") }))
	@ManyToOne(fetch = EAGER, targetEntity = UMTenant.class)
	@JoinColumn(name = "TENANT_ID", referencedColumnName = "id")
	public UMTenant getUMTenant() {
		return (UMTenant) tenant;
	}

	public void setUMTenant(UMTenant tenant) {
		this.tenant = tenant;
	}

	@XMLIgnore
	@Transient
	// @XMLMapping(tagTypeMapping={"tenant=com.cnebula.um.ejb.saas.UMTenant"})
	public ITenant getTenant() {
		return tenant;
	}

	public void setTenant(ITenant tenant) {
		this.tenant = (UMTenant) tenant;
	}

	public Object clone() throws CloneNotSupportedException {
		Role role = (Role) super.clone();
		List userRulesList = new ArrayList();
		userRulesList.addAll(userRules);
		role.setUMUserRules(userRulesList);

		List permissionRulesList = new ArrayList();
		permissionRulesList.addAll(permissionRules);
		role.setUMPermissionRules(permissionRulesList);
		return role;
	}
}
