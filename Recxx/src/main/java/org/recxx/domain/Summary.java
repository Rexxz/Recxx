package org.recxx.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.recxx.utils.ComparisonUtils;

@Entity
@Table(name="RecxxSummary")
public class Summary implements Serializable {

	private static final long serialVersionUID = 4467773888173148278L;

	@Id
	@GeneratedValue
	private Integer summaryId;
	private String configName;
	private String subject;
	private Date businessDate;
	private Date reconciliationDate;
	private String alias1;
	private String alias2;
	private Integer alias1Count;
	private Integer alias2Count;
	private Integer matchCount;
	private BigDecimal alias1MatchPercent;
	private BigDecimal alias2MatchPercent;
	private Long alias1ExecutionTime;
	private Long alias2ExecutionTime;

	public Summary () {}

	private Summary(Builder builder) {
		this.configName = builder.configName;
		this.subject = builder.subject;
		this.businessDate = builder.businessDate;
		this.reconciliationDate = builder.reconciliationDate;
		this.alias1 = builder.alias1;
		this.alias2 = builder.alias2;
		this.alias1Count = builder.alias1Count;
		this.alias2Count = builder.alias2Count;
		this.matchCount = builder.matchCount;
		this.alias1MatchPercent = builder.alias1MatchPercent;
		this.alias2MatchPercent = builder.alias2MatchPercent;
		this.alias1ExecutionTime = builder.alias1ExecutionTime;
		this.alias2ExecutionTime = builder.alias2ExecutionTime;
	}

	public static class Builder {

		String configName;
		String subject;
		Date businessDate;
		Date reconciliationDate;
		Integer alias1Count;
		Integer alias2Count;
		Integer matchCount;
		String alias1;
		String alias2;
		BigDecimal alias1MatchPercent;
		BigDecimal alias2MatchPercent;
		Long alias1ExecutionTime;
		Long alias2ExecutionTime;

		public Builder configName(String name) {
			configName = name;
			return this;
		}

		public Builder subject(String sub) {
			subject = sub;
			return this;
		}

		public Builder alias1(String alias) {
			alias1 = alias;
			return this;
		}

		public Builder alias2(String alias) {
			alias2 = alias;
			return this;
		}

		public Builder businessDate(Date date) {
			businessDate = date;
			return this;
		}

		public Builder alias1Count(Integer count) {
			alias1Count = count;
			return this;
		}

		public Builder alias2Count(Integer count) {
			alias2Count = count;
			return this;
		}

		public Builder alias1ExecutionTime(Long timeMillis) {
			alias1ExecutionTime = timeMillis;
			return this;
		}

		public Builder alias2ExecutionTime(Long timeMillis) {
			alias2ExecutionTime = timeMillis;
			return this;
		}

		private void calcPerecntages() {
			if (alias1Count != null && alias2Count != null) {
				alias1MatchPercent = ComparisonUtils.percentageMatch(matchCount, alias1Count);
				alias2MatchPercent = ComparisonUtils.percentageMatch(matchCount, alias2Count);
			}
		}

		public Builder matchCount(Integer count) {
			matchCount = count;
			return this;
		}

		public Summary build() {
			if (reconciliationDate == null) {
				reconciliationDate = new Date();
			}
			calcPerecntages();
			return new Summary(this);
		}
	}

	public Integer getSummaryId() {
		return summaryId;
	}

	public void setSummaryId(Integer summaryId) {
		this.summaryId = summaryId;
	}

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Date getReconciliationDate() {
		return reconciliationDate;
	}

	public void setReconciliationDate(Date reconciliationDate) {
		this.reconciliationDate = reconciliationDate;
	}

	public String getAlias1() {
		return alias1;
	}

	public void setAlias1(String alias1) {
		this.alias1 = alias1;
	}

	public String getAlias2() {
		return alias2;
	}

	public void setAlias2(String alias2) {
		this.alias2 = alias2;
	}

	public Date getBusinessDate() {
		return businessDate;
	}

	public void setBusinessDate(Date businessDate) {
		this.businessDate = businessDate;
	}

	public Integer getAlias1Count() {
		return alias1Count;
	}

	public void setAlias1Count(Integer alias1Count) {
		this.alias1Count = alias1Count;
	}

	public Integer getAlias2Count() {
		return alias2Count;
	}

	public void setAlias2Count(Integer alias2Count) {
		this.alias2Count = alias2Count;
	}

	public Integer getMatchCount() {
		return matchCount;
	}

	public void setMatchCount(Integer matchCount) {
		this.matchCount = matchCount;
	}

	public BigDecimal getAlias1MatchPercent() {
		return alias1MatchPercent;
	}

	public void setAlias1MatchPercent(BigDecimal alias1MatchPercent) {
		this.alias1MatchPercent = alias1MatchPercent;
	}

	public BigDecimal getAlias2MatchPercent() {
		return alias2MatchPercent;
	}

	public void setAlias2MatchPercent(BigDecimal alias2MatchPercent) {
		this.alias2MatchPercent = alias2MatchPercent;
	}

	public Long getAlias1ExecutionTime() {
		return alias1ExecutionTime;
	}

	public void setAlias1ExecutionTime(Long executionTime) {
		this.alias1ExecutionTime = executionTime;
	}

	public Long getAlias2ExecutionTime() {
		return alias2ExecutionTime;
	}

	public void setAlias2ExecutionTime(Long executionTime) {
		this.alias2ExecutionTime = executionTime;
	}

	public String toOutputString(){
		return toOutputString(Default.COMMA, Default.LINE_DELIMITER, Default.PERCENT_FORMAT);
	}

	public String toOutputString(String delimiter, String lineDelimiter, DecimalFormat percentFormat) {
		StringBuilder sb = new StringBuilder();
		sb.append(lineDelimiter)
		.append(lineDelimiter)
		.append("ConfigName, ").append(getConfigName()).append(lineDelimiter)
		.append("Subject, ").append(getSubject()).append(lineDelimiter)
		.append("BusinessDate, ").append(getBusinessDate()).append(lineDelimiter)
		.append(lineDelimiter)
		.append("======================").append(lineDelimiter)
		.append("Reconciliation Summary").append(lineDelimiter)
		.append("======================").append(lineDelimiter)
		.append(getAlias1()).append(" rows").append(delimiter).append(getAlias1Count()).append(lineDelimiter)
		.append(getAlias2()).append(" rows").append(delimiter).append(getAlias2Count()).append(lineDelimiter)
		.append(getAlias1()).append(" matched ").append(getAlias2()).append(delimiter).append(getMatchCount()).append(lineDelimiter)
		.append(getAlias1()).append(" matched ").append(getAlias2()).append(delimiter).append(Default.PERCENT_FORMAT.format(getAlias1MatchPercent())).append(lineDelimiter)
		.append(getAlias2()).append(" matched ").append(getAlias1()).append(delimiter).append(Default.PERCENT_FORMAT.format(getAlias2MatchPercent())).append(lineDelimiter)
		.append(getAlias1()).append(" time(ms) ").append(delimiter).append(getAlias1ExecutionTime()).append(lineDelimiter)
		.append(getAlias2()).append(" time(ms) ").append(delimiter).append(getAlias2ExecutionTime()).append(lineDelimiter);
		return sb.toString();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public boolean equals(Object thatObject) {
        if (this == thatObject) {
            return true;
        }
        if (!(thatObject instanceof Summary)) {
            return false;
        }
        Summary that = (Summary) thatObject;
        return summaryId == null ? that.summaryId == null : summaryId.equals(that.summaryId);
    }

	@Override
    public int hashCode() {
        return summaryId * 17;
    }


}
