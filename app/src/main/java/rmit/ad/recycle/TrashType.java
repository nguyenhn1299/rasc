package rmit.ad.recycle;



public enum TrashType  {

    VOCO("Vô cơ", "", ""), HUUCO("Hữu cơ", "", ""), TAICHE("Tái chế", "", "Hdfkjsfkadfkshfjjsfskfjhdsfsfhhj");

    TrashType(String name, String info, String image) {
        this.name = name;
        this.info = info;
        this.image = image;
    }

    public String name;
    public String info;
    public String image;
}
