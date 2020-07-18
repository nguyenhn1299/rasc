package rmit.ad.recycle;



public enum TrashType  {

    VOCO("Vô cơ", "hay bo rac vo co vao thung", R.drawable.voco), HUUCO("Hữu cơ", "hay bo rac huu co vao thung", R.drawable.huuco), TAICHE("Tái chế", "hay bo rac tai che vao thung", R.drawable.plastic),
    PIN("Pin","hay bo pin vao thung",R.drawable.pin), DIENTU("Dien tu","hay bo rac dien tu vao thung",R.drawable.dien);

    TrashType(String name, String info, int image) {
        this.name = name;
        this.info = info;
        this.image = image;
    }

    public String name;
    public String info;
    public int image;
}
