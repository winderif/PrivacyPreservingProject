package util;

public class Mode {
	public static final int NONE = 0;
	public static final int START = 1;
	public static final int SEND_KEY = 2;
	public static final int SEND_KEY_ACK = 3;
	public static final int SEND_QUERY = 4;
	public static final int COMP_SCORE_C = 5;
	public static final int COMP_SCORE_S = 6;
	public static final int COMPUTED_SCORE = 7;
	public static final int CP_REDUCE_C = 8;
	public static final int CP_MASK_S = 9;
	public static final int CP_CHECK_C = 10;
	public static final int CP_GET_LAMBDA_S = 11;
	public static final int CP_FIND_MIN_C = 12;
	public static final int CP_GET_MIN_S = 13;
	public static final int BEST_MATCHED = 15;	
	private int mMode;
	public Mode() {
		this.mMode = NONE;
	}	
	public Mode(int m) {
		this.mMode = m;
	}
	
	public void setMode(int mode) { this.mMode = mode; }	
	public int getMode() { return this.mMode; }
	
	public boolean isStart() {
		return (getMode()==START)?(true):(false);
	}
	public boolean isSendPublicKey() {
		return (getMode()==SEND_KEY)?(true):(false);
	}
	public boolean isSendPublicKeyAlready() {
		return (getMode()==SEND_KEY_ACK)?(true):(false);
	}
	public boolean isSendQueryDatas() {
		return (getMode()==SEND_QUERY)?(true):(false);
	}
	public boolean isComputingScoreOnClient() {
		return (getMode()==COMP_SCORE_C)?(true):(false);
	}
	public boolean isComputingScoreOnServer() {
		return (getMode()==COMP_SCORE_S)?(true):(false);
	}
	public boolean isComputedScore() {
		return (getMode()==COMPUTED_SCORE)?(true):(false);
	}
	public boolean isReduceDmodLOnClient() {
		return (getMode()==CP_REDUCE_C)?(true):(false);
	}
	public boolean isMaskOnServer() {
		return (getMode()==CP_MASK_S)?(true):(false);
	}
	public boolean isCheckWhetherOneOfZero() {
		return (getMode()==CP_CHECK_C)?(true):(false);
	}
	public boolean isGetLambdaOnServer() {
		return (getMode()==CP_GET_LAMBDA_S)?(true):(false);
	}
	public boolean isFindMinimumOfTwoOnClient() {
		return (getMode()==CP_FIND_MIN_C)?(true):(false);
	}
	public boolean isGetMinimumValueOnServer() {
		return (getMode()==CP_GET_MIN_S)?(true):(false);
	}
	public boolean isFindBestMatchingResult() {
		return (getMode()==BEST_MATCHED)?(true):(false);
	}
}
