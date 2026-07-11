document.addEventListener('DOMContentLoaded', () => {

    const botonMenuMovil = document.getElementById('botonMenuMovil');
    const menuLateral = document.getElementById('menuLateral');

    if (botonMenuMovil && menuLateral) {

        botonMenuMovil.addEventListener('click', () => {

            menuLateral.classList.toggle('hidden');

        });

    }

});