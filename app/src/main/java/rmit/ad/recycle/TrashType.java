package rmit.ad.recycle;



public enum TrashType  {

    VOCO("Vô cơ", "Rác vô cơ là những loại rác không thể sử dụng được nữa cũng không thể tái chế được mà chỉ có thể xử lý bằng cách mang ra các khu chôn lấp rác thải. Nó bắt nguồn từ các loại vật liệu xây dựng không thẻ sử dụng hoặc đã qua sử dụng và được bỏ đi; các loại bao bì bọc bên ngoài hộp/ chai thực phẩm; các loại túi nilong được bỏ đi sau khi con người dùng đựng thực phẩm và một số loại vật dụng/ thiết bị trong đời sống hàng ngày của con người.", R.drawable.voco2),
    HUUCO("Hữu cơ", "Rác hữu cơ là loại rác dễ phân hủy và có thể tái chế để đưa vào sử dụng cho việc chăm bón và làm thức ăn cho động vật. Nó có nguồn gốc từ phần bỏ đi của thực phẩm sau khi lấy đi phần chế biến được thức ăn cho con người; phần thực phẩm thừa hoặc hư hỏng không thể sử dụng cho con người; các loại hoa, lá cây, cỏ không được con người sử dụng sẽ trở thành rác thải trong môi trường.", R.drawable.huuco),
    TAICHE("Tái chế", "Rác tái chế là loại rác khó phân hủy nhưng có thể đưa vào tái chế để sử dụng nhằm mục đích phục vụ cho con người. Ví dụ như các loại giấy thải, các loại hộp/ chai/ vỏ lon thực phẩm bỏ đi,..." +
            "Tái thế là chìa khóa dẫn đến thành công trong việc giảm thiểu chất thải hiện đại và là thành phần trong mô hình phân loại rác hiện nay bao gồm: giảm thiểu, tái sử dụng, tái chế.", R.drawable.plastic),
    PIN("Pin","Các kim loại nặng có trong pin như chì, kẽm, cadmium và thủy ngân sẽ thấm sâu vào lòng đất, gây ô nhiễm nguồn nước ngầm. Còn nếu đốt pin, các thành phần nguy hại đó sẽ bốc lên cao tạo thành khói độc gây ô nhiễm bầu không khí. Điều đáng nói ở đây là mỗi kim loại nặng trong cục pin nếu không được xử lý đúng cách sẽ dẫn đến nhiều vấn đề nghiêm trọng hơn ta nghĩ. ",R.drawable.pin),
    DIENTU("Điện tử","Thu hồi và tái chế miễn phí các sản phẩm điện tử bị lỗi hoặc đã qua sử dụng nhằm mục đích đảm bảo quy trình tái chế rác thải điện tử an toàn và thân thiện với môi trường. Các sản phẩm điện tử bị lỗi hoặc đã qua sử dụng sẽ được chương trình thu gom và xử lý theo một quy trình chuyên nghiệp chuyên nghiệp và thân thiện với môi trường nhằm đảm bảo tối đa hoá lượng tài nguyên thiên nhiên thu hồi được sau tái chế.",R.drawable.dien);

    TrashType(String name, String info, int image) {
        this.name = name;
        this.info = info;
        this.image = image;
    }

    public String name;
    public String info;
    public int image;
}
