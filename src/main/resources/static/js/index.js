
const images =  [
    "banner2.png",
    "banner3.png",
    "banner4.png"
];


let index = 0; 
let interval = null; 

interval = setInterval(chuyenTiep, 4000);

function chuyenTiep() {
    const imgMain = document.getElementById('imgMain');
    index = (index === images.length - 1) ? 0 : index + 1; 
    imgMain.setAttribute('src', `../img/${images[index]}`);
}


