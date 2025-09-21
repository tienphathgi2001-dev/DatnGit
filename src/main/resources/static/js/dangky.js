const btnRegister = document.getElementById('btnRegister');
btnRegister.addEventListener("click", handleRegister);

function handleRegister(event) {
    event.preventDefault();  // Ngăn chặn xảy ra hành động mặc định của sự kiện 

    const registerForm = document.getElementById('registerForm').elements;
    const username = registerForm.username;
    const firstname = registerForm.firstname;
    const lastname = registerForm.lastname;
    const password = registerForm.password;
    const confirmPassword = registerForm.confirmPassword;
    const email = registerForm.email;

    const errUsername = document.getElementById('errUsername');
    const errPassword = document.getElementById('errPassword');
    const errConfirmPassword = document.getElementById('errConfirmPassword');
    const errEmail = document.getElementById('errEmail');
    const errFirstname = document.getElementById('errFirstname');
    const errLastname = document.getElementById('errLastname');

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

    // Kiểm tra tên người dùng
    if (firstname?.value.trim() === "") {
        errFirstname.style.display = "block";
        errFirstname.innerText = "Tên người dùng không được để trống";
        isValid = false;
    } else {
        errFirstname.style.display = "none";
    }

    // Kiểm tra họ người dùng
    if (lastname?.value.trim() === "") {
        errLastname.style.display = "block";
        errLastname.innerText = "Họ người dùng không được để trống";
        isValid = false;
    } else {
        errLastname.style.display = "none";
    }

    // Kiểm tra mật khẩu
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*]).{8,}$/;
    if (password?.value.trim() === "") {
        errPassword.style.display = "block";
        errPassword.innerText = "Mật khẩu không được để trống";
        isValid = false;
    } else if (password.value.length < 8) {
        errPassword.style.display = "block";
        errPassword.innerText = "Mật khẩu phải có tối thiểu 8 ký tự";
        isValid = false;
    } else if (!passwordPattern.test(password.value)) {
        errPassword.style.display = "block";
        errPassword.innerText = "Mật khẩu phải gồm có chữ hoa, chữ thường và một số kí tự đặc biệt (!@#$%^&*)";
        isValid = false;
    } else {
        errPassword.style.display = "none";
    }

    // Kiểm tra mật khẩu xác nhận
    if (confirmPassword?.value.trim() === "") {
        errConfirmPassword.style.display = "block";
        errConfirmPassword.innerText = "Mật khẩu xác nhận không được để trống";
        isValid = false;
    } else if (password.value !== confirmPassword.value) {
        errConfirmPassword.style.display = "block";
        errConfirmPassword.innerText = "Mật khẩu xác nhận không khớp";
        isValid = false;
    } else {
        errConfirmPassword.style.display = "none";
    }

    // Kiểm tra email
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (email?.value.trim() === "") {
        errEmail.style.display = "block";
        errEmail.innerText = "Email không được để trống";
        isValid = false;
    } else if (!emailPattern.test(email.value)) {
        errEmail.style.display = "block";
        errEmail.innerText = "Email không hợp lệ";
        isValid = false;
    } else {
        errEmail.style.display = "none";
    }

    if (isValid) {
        alert('Tài khoản đã được tạo thành công!');
    }
}
