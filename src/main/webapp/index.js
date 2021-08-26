async function login(e) {
    e.preventDefault();
    let data = new FormData(document.getElementById('login-form'));
    let username = data.get('username');
    let password = data.get('password');
    let resp = await authUser(username, password);
    if (resp.status === 200) {
        location.href = `/paypass/account.html?username=${username}`;
    } else {
        let errorMsg = await resp.text();
        alert(errorMsg);
    }
}

async function authUser(name, password) {
    let response = await fetch('http://localhost:8080/paypass/login/auth', {
        method: "POST",
        headers: {'content-type': 'application/json'},
        body: JSON.stringify({
            userName: name,
            password: password
        })
    });
    return response;
}

document.getElementById('login-form').onsubmit = login;
