const $ = (selectId) => document.getElementById(selectId);

async function pay() {
    const from = $('user-name').innerText;
    const to = $("to-name").value;
    const amount = $("pay-amount").value;
    let response = await fetch('http://localhost:8080/paypass/account/pay', {
        method: "POST",
        headers: {'content-type': 'application/json'},
        body: JSON.stringify({
            fromName: from,
            toName: to,
            amount: amount
        })
    });
    const accountInfo = await response.json();
    $('balance').innerText = accountInfo.balance || "0";
    $('debt').innerText = accountInfo.debt || "0";
    $('update-date').innerText = new Date().toLocaleString();
    $('creditor').innerText = accountInfo.creditor;
    console.warn("test topup" + Date());
}

async function topup() {
    const userName = $('user-name').innerText;
    const amount = $('topup-amount').value;
    let response = await fetch('http://localhost:8080/paypass/account/topup', {
        method: "POST",
        headers: {'content-type': 'application/json'},
        body: JSON.stringify({
            name: userName,
            amount: amount
        })
    });
    const accountInfo = await response.json();
    $('balance').innerText = accountInfo.balance || "0";
    $('debt').innerText = accountInfo.debt || "0";
    $('update-date').innerText = new Date().toLocaleString();
    $('creditor').innerText = accountInfo.creditor;
    console.warn("test topup" + Date().toLocaleString());
}

(async () => {
    let name = new URL(window.location.href).searchParams.get('username');
    $('user-name').innerText = name;
    let resp = await fetch(`http://localhost:8080/paypass/account/${name}`);
    const accountInfo = await resp.json();
    $('balance').innerText = accountInfo.balance || "0";
    $('debt').innerText = accountInfo.debt || "0";
    $('creditor').innerText = accountInfo.creditor;
    $('update-date').innerText = new Date().toLocaleString();

    $('pay').addEventListener('click', () => pay())
    $('topup').addEventListener('click', () => topup())
})();

