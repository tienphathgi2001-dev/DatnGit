document.addEventListener("DOMContentLoaded", function() {
    const productsContainer = document.getElementById("products");
    const additionalContentContainer = document.getElementById("additional-content");

    // chia mảng dữ liệu làm 2 phần 1 trên và 1 dưới
    const firstHalf = data.slice(0, 3);
    const secondHalf = data.slice(3);

   //html của sản phẩn ở mảng dữ liệu trên
    const htmlDataFirstHalf = firstHalf.map(function(value) {
        return `<div class="new">
            <ul>
                <a href="sanpham.html?id=${value.id}"><img id="imgSp${value.id}" src="../img/${value.images[0]}" alt="${value.name}" style="width: 350px;"></a>
            </ul>
            <ul>
                <h3><strong>${value.name}</strong></h3>
                <h4>${value.price} đ</h4>
            </ul>
        </div>`;
    });

   //html của sản phẩn ở mảng dữ liệu dưới
    const htmlDataSecondHalf = secondHalf.map(function(value) {
        return `<div class="new">
            <ul>
                <a href="sanpham.html?id=${value.id}"><img id="imgSp${value.id}" src="../img/${value.images[0]}" alt="${value.name}" style="width: 350px;"></a>
            </ul>
            <ul>
                <h3><strong>${value.name}</strong></h3>
                <h4>${value.price} đ</h4>
            </ul>
        </div>`;
    });

    // chèn dữ liệu vào vùng chứa tương ứng
    productsContainer.innerHTML = htmlDataFirstHalf.join("");
    additionalContentContainer.innerHTML = htmlDataSecondHalf.join("");
});
