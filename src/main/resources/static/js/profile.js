// profile.js

document.addEventListener('DOMContentLoaded', function () {
    // Xử lý sự kiện tìm kiếm (tương tự trang index)
    const searchIcon = document.getElementById('searchIcon');
    const searchForm = document.getElementById('searchForm');
    const searchInput = searchForm.querySelector('.search-input');

    searchIcon.addEventListener('click', function () {
        searchForm.classList.toggle('d-none');
        if (!searchForm.classList.contains('d-none')) {
            searchInput.focus(); // Tự động focus vào input khi mở form
        }
    });

    searchForm.addEventListener('submit', function (e) {
        e.preventDefault();
        const searchQuery = searchInput.value.trim();
        if (searchQuery) {
            window.location.href = `/search?query=${encodeURIComponent(searchQuery)}`;
        }
    });

    // Hiệu ứng hover cho avatar
    const profileImage = document.querySelector('.img-fluid.rounded-circle');
    if (profileImage) {
        profileImage.addEventListener('mouseenter', function () {
            this.style.transform = 'scale(1.05)';
        });
        profileImage.addEventListener('mouseleave', function () {
            this.style.transform = 'scale(1)';
        });
    }

    // Hiệu ứng smooth scroll khi click vào liên kết trong navbar
    document.querySelectorAll('.navbar-nav .nav-link').forEach(link => {
        link.addEventListener('click', function (e) {
            if (this.getAttribute('href').startsWith('/')) {
                e.preventDefault();
                const targetUrl = this.getAttribute('href');
                document.querySelector('body').scrollIntoView({ behavior: 'smooth', block: 'start' });
                window.location.href = targetUrl;
            }
        });
    });

    // Hiệu ứng fade-in cho card profile khi tải trang
    const profileCard = document.querySelector('.card');
    if (profileCard) {
        profileCard.style.opacity = '0';
        setTimeout(() => {
            profileCard.style.transition = 'opacity 0.8s ease-in';
            profileCard.style.opacity = '1';
        }, 100);
    }

    // Thêm hiệu ứng cho nút "Quay lại"
    const backButton = document.querySelector('.btn-secondary');
    if (backButton) {
        backButton.addEventListener('mouseenter', function () {
            this.style.transform = 'scale(1.05)';
            this.style.backgroundColor = '#5a6268';
        });
        backButton.addEventListener('mouseleave', function () {
            this.style.transform = 'scale(1)';
            this.style.backgroundColor = '#6c757d';
        });
    }
});

// Thêm hiệu ứng marquee mượt mà hơn
document.querySelector('marquee').style.animation = 'slide 15s linear infinite';