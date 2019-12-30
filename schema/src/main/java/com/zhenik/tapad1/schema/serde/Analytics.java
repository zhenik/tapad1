package com.zhenik.tapad1.schema.serde;

import java.util.HashSet;
import java.util.Set;

public class Analytics {
  private Long clicks;
  private Long impressions;
  private Set<String> users;

  public Analytics() {
    this.clicks = 0L;
    this.impressions = 0L;
    this.users = new HashSet<>();
  }

  public Analytics(Long clicks, Long impressions, Set<String> users) {
    this.clicks = clicks;
    this.impressions = impressions;
    this.users = users;
  }

  public Long getClicks() {
    return clicks;
  }

  public void setClicks(Long clicks) {
    this.clicks = clicks;
  }

  public Long getImpressions() {
    return impressions;
  }

  public void setImpressions(Long impressions) {
    this.impressions = impressions;
  }

  public Set<String> getUsers() {
    return users;
  }

  public void setUsers(Set<String> users) {
    this.users = users;
  }

  public void incrementClicks() {
    this.clicks+=1;
  }

  public void incrementImpressions() {
    this.impressions+=1;
  }

  public void addUser(String user) {
    this.users.add(user);
  }

  @Override public String toString() {
    return "Analytics{" +
        "clicks=" + clicks +
        ", impressions=" + impressions +
        ", users=" + users +
        '}';
  }

  public String view() {
    return "clicks=" + clicks +
        ", impressions=" + impressions +
        ", users=" + users.size();
  }
}
