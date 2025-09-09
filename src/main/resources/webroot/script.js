function sendGreeting() {
    const name = document.getElementById("name").value || "World";

    fetch(`/app/greeting?name=${encodeURIComponent(name)}`)
        .then(response => response.text())
        .then(data => {
            document.getElementById("greetResponse").innerText = data;
        })
        .catch(error => {
            document.getElementById("greetResponse").innerText = "Error de conexión";
            console.error(error);
        });
}

function getFactors() {
    const num = document.getElementById("factorsNumber").value;

    if (!num) {
        document.getElementById("factorsResponse").innerText = "Por favor ingresa un número";
        return;
    }

    fetch(`/app/factors?number=${encodeURIComponent(num)}`)
        .then(response => response.text())
        .then(data => {
            document.getElementById("factorsResponse").innerText = "Factores primos: " + data;
        })
        .catch(error => {
            document.getElementById("factorsResponse").innerText = "Error de conexión";
            console.error(error);
        });
}

function getSquare() {
    const num = document.getElementById("squareNumber").value;

    if (!num) {
        document.getElementById("squareResponse").innerText = "Por favor ingresa un número";
        return;
    }

    fetch(`/app/square?number=${encodeURIComponent(num)}`)
        .then(response => response.text())
        .then(data => {
            document.getElementById("squareResponse").innerText = "Cuadrado: " + data;
        })
        .catch(error => {
            document.getElementById("squareResponse").innerText = "Error de conexión";
            console.error(error);
        });
}
