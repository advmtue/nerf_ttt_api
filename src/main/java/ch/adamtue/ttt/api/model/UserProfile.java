package ch.adamtue.ttt.api.model;

public class UserProfile {
	private String pk;
	private String sk;
	private String user;
	private String displayName;
	private long statsKills;
	private long statsDeaths;
	private long statsWins;
	private long statsLosses;
	private String accessRole;
	private long joinDate;

	// Accessor / Mutator
	public String getPK() { return pk; }
	public void setPK(String pk) {
		this.pk = pk;
	}

	public String getSK() { return sk; }
	public void setSK(String sk) {
		this.sk = sk;
	}

	public String getUser() { return user; }
	public void setUser(String user) {
		this.user = user;
	}

	public String getDisplayName() { return displayName; }
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public long getStatsKills() { return statsKills; }
	public void setStatsKills(long statsKills) {
		this.statsKills = statsKills;
	}

	public long getStatsDeaths() { return statsDeaths; }
	public void setStatsDeaths(long statsDeaths) {
		this.statsDeaths = statsDeaths;
	}

	public long getStatsWins() { return statsWins; }
	public void setStatsWins(long statsWins) {
		this.statsWins = statsWins;
	}

	public long getStatsLosses() { return statsLosses; }
	public void setStatsLosses(long statsLosses) {
		this.statsLosses = statsLosses;
	}

	public String getAccessRole() { return accessRole; }
	public void setAccessRole(String accessRole) {
		this.accessRole = accessRole;
	}

	public long getJoinDate() { return joinDate; }
	public void setJoinDate(long joinDate) {
		this.joinDate = joinDate;
	}
}
