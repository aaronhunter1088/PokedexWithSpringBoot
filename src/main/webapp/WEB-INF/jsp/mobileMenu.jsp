<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!-- Mobile Header -->
<div class="mobile-header ${isDarkMode?'darkmode':'lightmode'}">
    <div class="pokemon-logo">
        <a href="${pageContext.request.contextPath}/search" title="Search">
            <img alt="pokedex" src="${pageContext.request.contextPath}/images/pokedex.png">
        </a>
    </div>
    <button class="mobile-menu-button" onclick="toggleMobileMenu();" aria-label="Menu">
        <i class="fa-solid fa-ellipsis-vertical"></i>
    </button>
</div>

<!-- Mobile Menu Overlay -->
<div class="mobile-menu-overlay" id="mobileMenuOverlay" onclick="closeMobileMenu();"></div>

<!-- Mobile Menu -->
<div class="mobile-menu ${isDarkMode?'darkmode':'lightmode'}" id="mobileMenu">
    <button class="mobile-menu-close" onclick="closeMobileMenu();" aria-label="Close menu">
        <i class="fa-solid fa-times"></i>
    </button>

    <div class="mobile-menu-item mobile-gif-item">
        <label>Show GIFs</label>
        <label class="switch" title="If GIF is not present, official artwork will show!">
            <input id="gifSwitchMobile" type="checkbox" ${showGifs ? 'checked' : ''}
                   onclick="toggleGifs();">
            <span class="slider round"></span>
        </label>
    </div>

    <div class="mobile-menu-item mobile-gif-item">
        <label class="mobile-darkmode-label">${isDarkMode ? 'Dark Mode' : 'Light Mode'}</label>
        <div id="mobileThemeToggle" class="theme-toggle ${isDarkMode ? 'dark' : ''}">
            <img src="images/sun.png" alt="Switch to dark mode" class="sun"
                 title="Switch to dark mode" onclick="toggleDarkmodeMobile();">
            <img src="images/moon.png" alt="Switch to light mode" class="moon"
                 title="Switch to light mode" onclick="toggleDarkmodeMobile();">
        </div>
    </div>

    <div class="mobile-menu-item">
        <div class="search-box" style="--search-shadow-color:${tileColorParam}; --search-input-color:${tileColorParam};">
            <input id="searchMobile" name="searchMobile" type="text" placeholder="Name or ID"/>
            <div class="search-icon" onclick="searchForPkmn();">
                <img alt="Get Pokémon"
                     src="${pageContext.request.contextPath}/images/pokeball_search.png"
                     style="width:50px; height:50px; cursor: pointer;"
                     title="Search for Pkmn">
            </div>
        </div>
    </div>

    <div class="mobile-menu-item">
        <div class="mobile-search-box" style="--search-shadow-color:${tileColorParam}; background-color:${isDarkMode?'#1a1a1a':'whitesmoke'};">
            <input id="pageNumberMobile" name="pageNumberMobile" type="text" placeholder="Jump to Page"
                   style="color:${isDarkMode?'white':'black'};"/>
            <div class="mobile-search-icon" style="color:${isDarkMode?'white':'black'};"
                 onclick="setPageToViewMobile();" title="Jump to Page">
                <i class="fa-brands fa-page4" style="font-size:28px; cursor:pointer;"></i>
            </div>
        </div>
    </div>

    <div class="mobile-menu-item">
        <div class="mobile-search-box" style="--search-shadow-color:${tileColorParam}; background-color:${isDarkMode?'#1a1a1a':'whitesmoke'};">
            <input id="showPkmnNumberMobile" name="showPkmnNumberMobile" type="text" placeholder="Pok&#233;mon Per Page"
                   style="color:${isDarkMode?'white':'black'};"/>
            <div class="mobile-search-icon" style="color:${isDarkMode?'white':'black'};"
                 onclick="setPkmnPerPageMobile();" title="Show Pok&#233;mon">
                <i class="fa-solid fa-list-ol" style="font-size:28px; cursor:pointer;"></i>
            </div>
        </div>
    </div>

    <div class="mobile-menu-item">
        <button class="back-to-landing-btn icon" onclick="navigateToLandingPage()"
                style="background-color:${tileColorParam};"
                title="Return to Landing Page">
            Back to Landing Page
        </button>
    </div>
</div>

<script>
    const MOBILE_MENU_CLOSE_DURATION_MS = 2000;
    let mobileMenuCloseTimeoutId;

    $(function(){
        //checks whether the pressed key is "Enter"
        $('#searchMobile').on('keypress', function(e) {
            if ($('#searchMobile').val() === '') return;
            if (e.code === 'Enter' || e.code === 'Return') {
                searchForPkmn('${isDarkMode}');
            }
        });
        $("#gifSwitchMobile").prop("checked", '${showGifs}' === 'true');
    });

    function toggleMobileMenu() {
        const menu = document.getElementById('mobileMenu');
        const overlay = document.getElementById('mobileMenuOverlay');
        const isOpen = menu.classList.contains('active') && !menu.classList.contains('closing');
        if (isOpen) {
            closeMobileMenu();
            return;
        }
        clearTimeout(mobileMenuCloseTimeoutId);
        menu.classList.remove('closing');
        overlay.classList.remove('closing');
        menu.classList.add('active');
        overlay.classList.add('active');
        if (menu.classList.contains('active')) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = '';
        }
    }

    function closeMobileMenu() {
        const menu = document.getElementById('mobileMenu');
        const overlay = document.getElementById('mobileMenuOverlay');
        if (!menu.classList.contains('active')) {
            document.body.style.overflow = '';
            return;
        }
        clearTimeout(mobileMenuCloseTimeoutId);
        menu.classList.add('closing');
        overlay.classList.add('closing');
        mobileMenuCloseTimeoutId = setTimeout(() => {
            menu.classList.remove('active', 'closing');
            overlay.classList.remove('active', 'closing');
            document.body.style.overflow = '';
        }, MOBILE_MENU_CLOSE_DURATION_MS);
    }

    function toggleGifs() {
        $.ajax({
            type: "GET",
            url: "../toggleGifs",
            async: false,
            dataType: "application/json",
            crossDomain: true,
            statusCode: {
                200: function(data) {
                    updateGifToggle(false, data);
                },
                404: function() {
                    console.log('Failed');
                },
                500: function() {
                    console.log('Server Error');
                }
            }
        });
        setTimeout(() => {
            closeMobileMenu();
        }, 1000);
    }

    function toggleDarkmodeMobile() {
        const contextPath = "${pageContext.request.contextPath}";
        const currentIsDark = document.body.classList.contains("darkmode");
        const targetIsDark = !currentIsDark;
        console.log('toggling darkmode, target dark: ' + targetIsDark);
        const themeToggle = document.getElementById("mobileThemeToggle");
        const sunIcon = themeToggle.querySelector(".sun");
        const moonIcon = themeToggle.querySelector(".moon");
        const outgoingIcon = targetIsDark ? sunIcon : moonIcon;
        const incomingIcon = targetIsDark ? moonIcon : sunIcon;
        const rotation = targetIsDark ? 180 : -180;

        outgoingIcon.animate([
            { opacity: 1, transform: "rotate(0deg) scale(1)" },
            { opacity: 0, transform: "rotate(" + rotation + "deg) scale(0.5)" }
        ], { duration: 500, easing: "ease", fill: "forwards" });
        incomingIcon.animate([
            { opacity: 0, transform: "rotate(" + (-rotation) + "deg) scale(0.5)" },
            { opacity: 1, transform: "rotate(0deg) scale(1)" }
        ], { duration: 500, easing: "ease", fill: "forwards" });

        themeToggle.classList.toggle("dark", targetIsDark);
        sunIcon.style.opacity = targetIsDark ? "0" : "1";
        sunIcon.style.pointerEvents = targetIsDark ? "none" : "auto";
        moonIcon.style.opacity = targetIsDark ? "1" : "0";
        moonIcon.style.pointerEvents = targetIsDark ? "auto" : "none";

        $.ajax({
            type: "GET",
            url: contextPath + "/toggleDarkmode",
            async: false,
            dataType: "json",
            crossDomain: true,
            statusCode: {
                200: function(result) {
                    const isDark = result === true || result === "true";
                    console.log('toggleDarkmode: ' + isDark);
                    const $body = $('body');
                    $body.toggleClass('dark darkmode', isDark);
                    $body.toggleClass('light lightmode', !isDark);
                    if ($("#gifSwitchDarkmode").length) {
                        $("#gifSwitchDarkmode").prop("checked", isDark);
                    }
                    $(".mobile-darkmode-label").text(isDark ? 'Dark Mode' : 'Light Mode');
                    $(".mobile-header").toggleClass("darkmode", isDark).toggleClass("lightmode", !isDark);
                    $("#mobileMenu").toggleClass("darkmode", isDark).toggleClass("lightmode", !isDark);
                    themeToggle.classList.toggle("dark", isDark);
                    sunIcon.style.opacity = isDark ? "0" : "1";
                    sunIcon.style.pointerEvents = isDark ? "none" : "auto";
                    moonIcon.style.opacity = isDark ? "1" : "0";
                    moonIcon.style.pointerEvents = isDark ? "auto" : "none";
                    setTimeout(() => {
                        closeMobileMenu();
                    }, 1000);
                },
                404: function() {
                    console.log('Failed');
                },
                500: function() {
                    console.log('Server Error');
                }
            }
        });
    }

    function updateGifToggle(reload, data) {
        let showGifs;
        try {
            showGifs = JSON.parse(data.responseText);
        } catch (error) {
            showGifs = data;
        }
        console.log("showGifs: " + showGifs);
        $("#gifSwitchMobile").prop("checked", showGifs === 'true');
        // if (reload) {
        //     setTimeout(function() {
        //         location.reload();
        //     }, 500);
        // }
    }

    function searchForPkmn() {
        let nameOrId = $("#searchMobile").val().trim();
        if (nameOrId === '') {
            return alert('Name or ID is required');
        }
        console.log('nameOrId: ' + nameOrId);

        const contextPath = "${pageContext.request.contextPath}";
        const encodedNameOrId = encodeURIComponent(nameOrId);

        $.ajax({
            type: "GET",
            url: contextPath + "/pokemon/" + encodedNameOrId,
            dataType: "json"
        })
        .done(function() {
            // 2) Only on success, do your current "200 path" behavior
            const url = contextPath + "/pokedex/" + encodedNameOrId + "?darkmode=" + isDarkMode;
            console.log('Navigating to: ' + url);
            window.location.href = url;
        })
        .fail(function(xhr) {
            console.log(JSON.parse(JSON.stringify(xhr.responseText)));
            // 3) Branch exactly by status
            if (xhr.status === 400) {
                alert("Pok\u00e9mon not found. Please check the Name or ID and try again.");
            } else {
                alert("Pok\u00e9mon search failed. Please try again later.");
            }
        });
    }

    function setPkmnPerPageMobile() {
        let value = $("#showPkmnNumberMobile").val().trim();
        setPkmnPerPageImpl(value, true);
        closeMobileMenu();
    }

    function setPkmnPerPageImpl(value, isMobile) {
        console.log("show " + value + " pokemon");
        $.ajax({
            type: "GET",
            url: "/springboot/pkmnPerPage",
            data: {
                pkmnPerPage: value
            },
            async: false,
            dataType: "application/json",
            crossDomain: true,
            statusCode: {
                200: function(data) {
                    console.log(JSON.parse(JSON.stringify(data.responseText)));
                    if (isMobile) closeMobileMenu();
                    location.reload();
                },
                400: function(data) {
                    console.log(JSON.parse(JSON.stringify(data.responseText)));
                },
                404: function() {
                    console.log('Resource not found');
                },
                500: function() {
                    console.log('Server Error');
                }
            }
        });
    }

    function setPageToViewMobile() {
        let pageNumber = $('#pageNumberMobile').val().trim();
        if (pageNumber === undefined || pageNumber === '')
        {
            return alert('Page number is required');
        }
        console.log("page to view: " + pageNumber);
        $.ajax({
            type: "GET",
            url: "/springboot/page",
            data: {
                pageNumber: pageNumber
            },
            async: false,
            dataType: "application/json",
            crossDomain: true,
            statusCode: {
                200: function(data) {
                    console.log('200 setPageToView');
                    console.log(JSON.parse(JSON.stringify(data.responseText)));
                    location.reload();

                    let ids = "${pokemonIds}".replace(/[\[\]]/g, '').split(',').map(id => id.trim());
                    for(let i=0; i<ids.length; i++) {
                        let pokemonBox = document.getElementById("pokemon"+(ids[0])+"Box");
                        let currentColor = pokemonBox.style.backgroundColor;
                        pokemonBox.style.backgroundColor = changeColor(currentColor);
                    }
                },
                400: function(data) {

                    //console.log(JSON.parse(JSON.stringify(data.responseText)));
                },
                404: function() {
                    console.log('Resource not found');
                },
                500: function() {
                    console.log('Server Error');
                }
            }
        });
    }

    function getByPkmnType(selectObject) {
        let type = selectObject.value;

        // Show loading overlay if a type is selected (not "none")
        if (type !== 'none') {
            showLoadingOverlay(type);
        }

        $.ajax({
            type: "GET",
            url: "getPokemonByType",
            data: {
                chosenType: type
            },
            async: false,
            dataType: "application/json",
            crossDomain: true,
            statusCode: {
                200: function(data) {
                    console.log('200 chosenType');
                    // Navigate to homepage instead of reload to avoid duplicate fetching
                    window.location.href = '${pageContext.request.contextPath}/';
                },
                400: function(data) {
                    console.log(JSON.parse(JSON.stringify(data.responseText)));
                    hideLoadingOverlay();
                },
                404: function() {
                    console.log('Resource not found');
                    hideLoadingOverlay();
                },
                500: function() {
                    console.log('Server Error');
                    hideLoadingOverlay();
                }
            }
        });
        console.log(type);
    }

    function navigateToLandingPage() {
        const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
        const currentDarkMode = document.body.classList.contains("darkmode");
        let url = "";
        url = `${env}` !== "prod" && isMobile
            ? "http://" + window.location.hostname + ":4200?tileNumber=1&darkmode=" + currentDarkMode
            : "http://localhost:4200?tileNumber=1&darkmode=" + currentDarkMode;
        url = `${env}` === "production"
            ? "https://mypokedex.us?tileNumber=1&darkmode=" + currentDarkMode
            : url;
        window.location.href = url;
    }

</script>