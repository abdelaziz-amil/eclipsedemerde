package generate;

public class Client {

    private String name;
    private String addr;

    public Client() {
    }

    public Client(String name, String addr) {
        this.name = name;
        this.addr = addr;
    }


    public void setName(String name) {
        this.name = name;
    }
    public String getAddr() {
        return this.addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }


}
