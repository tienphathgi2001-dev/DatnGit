document.addEventListener("DOMContentLoaded", function() {
    const btnSubmit = document.getElementById('btnSubmit');
    btnSubmit.addEventListener("click", handleFormSubmit);

    function handleFormSubmit(event) {
        event.preventDefault();  // Ngăn chặn hành động mặc định của sự kiện

        const contactForm = document.getElementById('contactForm').elements;
        const fullName = contactForm['fullName'];
        const phone = contactForm['phone'];
        const email = contactForm['email'];
        const message = contactForm['message'];

        const errFullName = document.getElementById('errFullName');
        const errPhone = document.getElementById('errPhone');
        const errEmail = document.getElementById('errEmail');
        const errMessage = document.getElementById('errMessage');
        
        let isValid = true;

        // Xóa các thông báo lỗi trước đó
        document.querySelectorAll('.error-message').forEach(function(el) {
            el.textContent = '';
            el.style.display = 'none';
        });

        // Kiểm tra họ và tên
        if (fullName?.value.trim() === "") {
            errFullName.style.display = "block";
            errFullName.innerText = "Họ và tên không được để trống";
            isValid = false;
        } else {
            errFullName.style.display = "none";
        }

        // Kiểm tra số điện thoại
        const phonePattern = /^\d{10}$/;
        if (phone?.value.trim() === "") {
            errPhone.style.display = "block";
            errPhone.innerText = "Số điện thoại không được để trống";
            isValid = false;
        } else if (!phonePattern.test(phone.value)) {
            errPhone.style.display = "block";
            errPhone.innerText = "Số điện thoại phải gồm 10 chữ số";
            isValid = false;
        } else {
            errPhone.style.display = "none";
        }

        // Kiểm tra email
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
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

        // Kiểm tra lời nhắn
        if (message?.value.trim() === "") {
            errMessage.style.display = "block";
            errMessage.innerText = "Lời nhắn không được để trống";
            isValid = false;
        } else {
            errMessage.style.display = "none";
        }

        if (isValid) {
            alert('Tin nhắn của bạn đã được gửi!');
        }
    }
});
