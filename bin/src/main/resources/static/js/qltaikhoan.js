// Tạo một mảng để lưu danh sách tài khoản
let accounts = [];

// Hàm kiểm tra nếu một vai trò đã được chọn
function checkRole(role) {
    // Nếu 'Người Dùng' được chọn thì bỏ chọn 'Nhân Viên' và ngược lại
    if (role === 'user') {
        document.getElementById('adminRole').checked = false;
    } else if (role === 'admin') {
        document.getElementById('userRole').checked = false;
    }
}

// Hàm thêm tài khoản
function addAccount() {
    const accountID = document.getElementById('accountID').value;
    const username = document.getElementById('username').value;
    const role = document.getElementById('userRole').checked ? 'Người Dùng' : 'Nhân Viên';
    const fullname = document.getElementById('fullname').value;
    const phone = document.getElementById('phone').value;
    const email = document.getElementById('email').value;

    // Kiểm tra nếu tất cả các trường bắt buộc đã được điền
    if (accountID === '' || username === '' || fullname === '' || phone === '' || email === '') {
        alert('Vui lòng điền đầy đủ các trường yêu cầu.');
        return;
    }

    // Tạo một đối tượng tài khoản mới
    const newAccount = {
        accountID,
        username,
        role,
        fullname,
        phone,
        email
    };

    // Thêm tài khoản mới vào mảng
    accounts.push(newAccount);

    // Cập nhật bảng hiển thị tài khoản
    updateAccountTable();
    
    // Xóa các trường sau khi thêm tài khoản
    document.getElementById('accountForm').reset();
}

// Hàm cập nhật bảng hiển thị tài khoản
function updateAccountTable() {
    const tableBody = document.getElementById('accountTable').getElementsByTagName('tbody')[0];
    tableBody.innerHTML = ''; // Xóa nội dung cũ

    // Duyệt qua mảng accounts và thêm hàng mới cho mỗi tài khoản
    accounts.forEach((account, index) => {
        const row = tableBody.insertRow();

        const cellAccountID = row.insertCell(0);
        const cellUsername = row.insertCell(1);
        const cellRole = row.insertCell(2);
        const cellFullname = row.insertCell(3);
        const cellPhone = row.insertCell(4);
        const cellEmail = row.insertCell(5);

        cellAccountID.textContent = account.accountID;
        cellUsername.textContent = account.username;
        cellRole.textContent = account.role;
        cellFullname.textContent = account.fullname;
        cellPhone.textContent = account.phone;
        cellEmail.textContent = account.email;

        // Thêm một thuộc tính onclick vào mỗi hàng để chỉnh sửa tài khoản
        row.onclick = function() {
            editAccount(index);
        };
    });
}

// Hàm chỉnh sửa tài khoản
function editAccount(index) {
    const account = accounts[index];

    // Điền dữ liệu của tài khoản đã chọn vào form
    document.getElementById('accountID').value = account.accountID;
    document.getElementById('username').value = account.username;
    document.getElementById('fullname').value = account.fullname;
    document.getElementById('phone').value = account.phone;
    document.getElementById('email').value = account.email;

    if (account.role === 'Người Dùng') {
        document.getElementById('userRole').checked = true;
        document.getElementById('adminRole').checked = false;
    } else {
        document.getElementById('adminRole').checked = true;
        document.getElementById('userRole').checked = false;
    }

    // Xóa tài khoản khỏi mảng để chỉnh sửa lại
    accounts.splice(index, 1);
    updateAccountTable();
}

// Hàm xóa tài khoản
function deleteAccount() {
    const accountID = document.getElementById('accountID').value;
    
    // Kiểm tra nếu mã tài khoản tồn tại trong danh sách
    const index = accounts.findIndex(account => account.accountID === accountID);
    if (index === -1) {
        alert('Không tìm thấy tài khoản để xóa.');
        return;
    }

    // Xóa tài khoản khỏi mảng
    accounts.splice(index, 1);
    updateAccountTable();

    // Xóa các trường sau khi xóa tài khoản
    document.getElementById('accountForm').reset();
}
