
document.addEventListener("DOMContentLoaded", function() {
    const id = window.location.search.replace("?", "").split("=")[1];

    let product = null;
    for (let index = 0; index < data.length; index++) {
        if (data[index].id == id) {
            product = data[index];
            break;
        }
    }

    if (product) {
        console.log("product === ", product);

        const mainImage = document.getElementById("mainImage");
        const imagesList = document.getElementById("imagesList");
        const name = document.getElementById("name");
        const price = document.getElementById("price");

        name.innerText = product?.name;
        price.innerText = `${product?.price}`;

        mainImage.setAttribute("src", `../img/${product?.images[0]}`);

        const htmlImages = product?.images?.map(function(value) {
            return `<img class="thumbnail" style="width: 70px; height: 70px; margin-bottom: 10px; margin-left: 10px; margin-right: 10px;" 
                src="../img/${value}" alt="${product.name} thumbnail"/>`;
        }).join("");

        imagesList.innerHTML = htmlImages;

        //click/hover hình nhỏ hiện lên ảnh lớn
        imagesList.addEventListener("click", function(e) {
            if (e.target && e.target.tagName === "IMG") {
                mainImage.setAttribute("src", e.target.getAttribute("src"));
            }
        });

        imagesList.addEventListener("mouseover", function(e) {
            if (e.target && e.target.tagName === "IMG") {
                mainImage.setAttribute("src", e.target.getAttribute("src"));
            }
        });
    } 
});
