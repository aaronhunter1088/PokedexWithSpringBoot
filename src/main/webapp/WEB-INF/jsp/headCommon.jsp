<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="icon" type="image/x-icon" href="${pageContext.request.contextPath}/images/pokeball_tabs.png"/>

<!-- jQuery UI CSS -->
<link rel="stylesheet" href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">

<!-- Bootstrap (keep a single version: 3.4.1) -->
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap-theme.min.css">

<!-- Font Awesome -->
<%--<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css">--%>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/7.0.1/css/all.min.css" integrity="sha512-2SwdPD6INVrV/lHTZbO2nodKhrnDdJK9/kg2XD1r9uGqPo1cUbujc+IYdlYdEErWNu69gVcYgdxlmVmzTWnetw==" crossorigin="anonymous" referrerpolicy="no-referrer" />

<!-- Index / app styles -->
<link href="${pageContext.request.contextPath}/resources/css/gifSlider.css" type="text/css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/resources/css/pokemonGrid.css" type="text/css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/resources/css/pokedex.css" type="text/css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/resources/css/evolutions.css" type="text/css" rel="stylesheet">
<link href="${pageContext.request.contextPath}/resources/css/mobile.css" type="text/css" rel="stylesheet">

<!-- jQuery (must come before jquery-ui and Bootstrap JS) -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.7.1/jquery.min.js"
        integrity="sha512-v2CJ7UaYy4JwqLDIrZUI/4hqeoQieOmAZNXBeQyjo21dadnwR+8ZaIJVT8EE2iyI61OV8e6M8PP2/4hpQINQ/g=="
        crossorigin="anonymous" referrerpolicy="no-referrer"></script>

<!-- jQuery UI (after jQuery) -->
<script src="https://code.jquery.com/ui/1.12.1/jquery-ui.js"></script>

<!-- Bootstrap JS (after jQuery) -->
<script src="https://stackpath.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>

<!-- Add more common links and scripts here -->

<!-- Common styling -->
<style>
    html * {
        margin: 0;
    }
    html {
        background-position: center center;
        background-repeat: no-repeat;
        background-attachment: fixed;
        background-size: cover;
        height: 100%;
    }

    body {
        height: 100%;
        width: auto;
        display: inherit;
        margin: 8px;
        background-position: center center;
        background-repeat: no-repeat;
        background-attachment: fixed;
        background-size: cover;
        justify-content: space-evenly;
        text-align: center;
    }

    @media (max-width: 768px) {
        body {
            display: grid;
        }
    }

    .button {
        font-weight: bold;
    }

    .cursor {
        cursor: pointer;
    }

    .center {
        padding: 70px 0;
        text-align: center;
        vertical-align: middle;
    }

    h1 {
        /*padding: 70px 0;*/
        text-align: center;
        line-height: 1.5;
        display: inline-block;
        vertical-align: middle;
    }

    .lightmode {
        background-color: white;
        color: black;
    }
    .darkmode {
        background-color: black;
        color: white;
    }
    .lightmode input {
        color: black !important;
        background-color: white !important;
    }
    .darkmode input {
        color: white !important;
        -webkit-text-fill-color: white !important;
        caret-color: white !important;
        background-color: black !important;
    }
    /* Autofill: browser ignores background-color, only inset box-shadow works */
    .darkmode input:-webkit-autofill,
    .darkmode input:-webkit-autofill:hover,
    .darkmode input:-webkit-autofill:focus,
    .darkmode input:-webkit-autofill:active {
        -webkit-text-fill-color: white !important;
        -webkit-box-shadow: 0 0 0 1000px black inset !important;
        box-shadow: 0 0 0 1000px black inset !important;
        caret-color: white !important;
        transition: background-color 9999s ease-in-out 0s;
    }
    .darkmode .search-box,
    .darkmode .input-box {
        background-color: black !important;
    }
    .darkmode #jumpToPage .input-icon,
    .darkmode #showPokemon .input-icon {
        color: white !important;
    }
    .lightmode #jumpToPage .input-icon,
    .lightmode #showPokemon .input-icon {
        color: black !important;
    }
    .darkmode .pagination .page-link {
        background-color: transparent !important;
        border-color: transparent !important;
        color: #0d6efd !important;
    }
    .darkmode .pagination .page-item.disabled .page-link {
        background-color: transparent !important;
        color: #5aa2ff !important;
    }
    .lightmode .pagination .page-link {
        background-color: transparent !important;
        color: #0d6efd !important;
        border-color: transparent !important;
    }
    .pagination .page-link.current-page {
        background-color: #0d6efd !important;
        border-color: #0d6efd !important;
        color: white !important;
        font-weight: 700 !important;
        text-decoration: none !important;
    }
    .back-to-landing-btn {
        background-color: #4CAF50;
        color: white;
        border: none;
        padding: 10px 15px;
        text-decoration: none;
        font-size: 16px;
        cursor: pointer;
        border-radius: 4px;
        transition: background-color 0.3s ease;
        white-space: nowrap;
    }
</style>
