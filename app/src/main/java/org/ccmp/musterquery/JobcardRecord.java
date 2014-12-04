package org.ccmp.musterquery;

public class JobcardRecord {
    private int id;
    private String jobcard;
    private String issueDate;
    private String headOfHousehold;
    private String caste;

    public JobcardRecord(int id, String jobcard, String headOfHousehold, String issueDate, String caste) {
        this.id = id;
        this.jobcard = jobcard;
        this.headOfHousehold = headOfHousehold;
        this.issueDate = issueDate;
        this.caste = caste;
    }

    public int getId() {
        return id;
    }

    public String getJobcard() {
        return jobcard;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public String getHeadOfHousehold() {
        return headOfHousehold;
    }

    public String getCaste() {
        return caste;
    }

    @Override
    public String toString() {
        return jobcard;
    }
}
