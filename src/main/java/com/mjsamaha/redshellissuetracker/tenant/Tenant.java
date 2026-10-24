package com.mjsamaha.redshellissuetracker.tenant;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.mjsamaha.redshellissuetracker.common.BaseEntity;
import com.mjsamaha.redshellissuetracker.common.TenantStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {

	@NotBlank
	@Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,61}[a-z0-9]$")
	@Column(nullable = false, unique = true, length = 63)
	private String slug;

	@NotBlank
	@Size(max = 100)
	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TenantStatus status = TenantStatus.ACTIVE;

	// Branding
	@Size(max = 500)
	@Column(length = 500)
	private String logoUrl;

	@Pattern(regexp = "^#[0-9a-fA-F]{6}$")
	@Column(length = 7)
	private String primaryColor;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> themeJson;

	// Settings
	@Column(nullable = false)
	private boolean moderationDefault = true;

	@Column(nullable = false)
	private boolean requireReporterEmail = false;

	// Limits (starting defaults, tune per plan)
	@Column(nullable = false)
	private int maxIssuesPerDay = 200;

	@Column(nullable = false)
	private int maxApplications = 5;

	@Column(nullable = false)
	private long maxAttachmentBytes = 5_242_880L; // 5 MB

}
