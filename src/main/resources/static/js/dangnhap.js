const btnDN = document.getElementById('btnDN');
btnDN.addEventListener("click", handleRegister);

function handleRegister(event) {
    event.preventDefault();  

    const loginForm = document.getElementById('loginForm').elements;
    const username = loginForm.username;
    const password = loginForm.password;

    const errUsername = document.getElementById('errUsername');
    const errPassword = document.getElementById('errPassword');
    
    let isValid = true;

    // Xóa các thông báo lỗi trước đó
    document.querySelectorAll('.error-message').forEach(function(el) {
        el.textContent = '';
        el.style.display = 'none';
    });

    // Kiểm tra tên đăng nhập
    if (username?.value.trim() === "") {
        errUsername.style.display = "block";
        errUsername.innerText = "Tên đăng nhập không được để trống";
        isValid = false;
    } else {
        errUsername.style.display = "none";
    }

    // Kiểm tra mật khẩu
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$/;
    if (password?.value.trim() === "") {
        errPassword.style.display = "block";
        errPassword.innerText = "Mật khẩu không được để trống";
        isValid = false;
    } else {
        errPassword.style.display = "none";
    }

    if (isValid) {
        alert('Đăng nhập thành công thành công!');
    }
}
