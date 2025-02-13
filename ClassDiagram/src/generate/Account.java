package generate;

public class Account {

    private int id;
    private int overdraft;
    private int balance;

    public Account() {
    }

    public Account(int id, int overdraft, int balance) {
        this.id = id;
        this.overdraft = overdraft;
        this.balance = balance;
    }


    public void setId(int id) {
        this.id = id;
    }

    public void setOverdraft(int overdraft) {
        this.overdraft = overdraft;
    }
    public int getBalance() {
        return this.balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }


}
