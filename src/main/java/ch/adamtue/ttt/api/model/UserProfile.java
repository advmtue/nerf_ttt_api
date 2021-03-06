package ch.adamtue.ttt.api.model;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

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

	/**
	 * Create a PK for user profile
	 *
	 * @param userId User UUID
	 * @return User Profile PK
	 */
	public static Map<String, AttributeValue> createPK(String userId) {
		return new HashMap<String, AttributeValue>(Map.of(
				"pk", createHashKey(userId),
				"sk", createRangeKey()
		));
	}

	/**
	 * Create a DynamoDB hash key for User Profile
	 *
	 * @param userId User UUID
	 * @return DynamoDB Hash key for User Profile
	 */
	public static AttributeValue createHashKey(String userId) {
		return AttributeValue.builder().s(String.format("USER#%s", userId)).build();
	}

	/**
	 * Create a DynamoDB range key for User Profile
	 *
	 * @return DynamoDB range key for User Profile
	 */
	public static AttributeValue createRangeKey() {
		return AttributeValue.builder().s("profile").build();
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
