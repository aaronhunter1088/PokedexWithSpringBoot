<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <title>Pok&#233;dex Spring Boot</title>
        <jsp:include page="headCommon.jsp"/>
        <style>
            body {
                transition:
                    background-color 2s ease,
                    color 2s ease;
            }
            .theme-toggle {
                position: relative;
                width: 50px;
                height: 50px;
                cursor: pointer;
                transition: transform 5s ease;
            }

            .theme-toggle img {
                position: absolute;
                top: 0;
                left: 0;

                width: 50px;
                height: 50px;
                object-fit: contain;

                transition: transform 5s ease, opacity 2s ease;
            }

            .theme-toggle .sun {
                opacity: 1;
                pointer-events: auto;
                transform: rotate(180deg) scale(1);
            }

            /* Moon starts hidden and rotated. */
            .theme-toggle .moon {
                opacity: 0;
                pointer-events: none;
                transform: rotate(180deg) scale(1);
            }

            .theme-toggle.dark .sun {
                opacity: 0;
                pointer-events: none;
                transform: rotate(-180deg) scale(1);
            }

            .theme-toggle.dark .moon {
                opacity: 1;
                pointer-events: auto;
                transform: rotate(180deg) scale(1);
            }

            #loadingOverlay {
                display: none;
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background-color: rgba(0, 0, 0, 0.7);
                z-index: 9999;
                justify-content: center;
                align-items: center;
            }
            
            #loadingContent {
                background-color: white;
                padding: 40px 60px;
                border-radius: 10px;
                text-align: center;
                box-shadow: 0 4px 6px rgba(0, 0, 0, 0.3);
            }
            
            #loadingContent h2 {
                margin: 0 0 20px 0;
                color: #333;
                font-size: 24px;
            }
            
            #loadingContent p {
                margin: 0;
                color: #666;
                font-size: 16px;
            }
            
            .spinner {
                border: 4px solid #f3f3f3;
                border-top: 4px solid #3498db;
                border-radius: 50%;
                width: 40px;
                height: 40px;
                animation: spin 1s linear infinite;
                margin: 20px auto 0 auto;
            }
            
            @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
            }
        </style>
    </head>

    <body class="${isDarkMode?'darkmode':'lightmode'}">
        <!-- Loading Overlay -->
        <div id="loadingOverlay">
            <div id="loadingContent">
                <h2 id="loadingHeader"></h2>
                <p>One moment please...</p>
                <div class="spinner"></div>
            </div>
        </div>
        
        <jsp:include page="mobileMenu.jsp"/>
        
        <!-- Desktop Header -->
        <div id="desktopHeader" style="display:flex;flex-direction:column;align-items:center;justify-content:center;width:100%;margin:0 auto;text-align:center;">
            <h1 id="indexSearchImgSearchLink" style="vertical-align:middle;display:flex;justify-content:center;width:100%;margin:0 auto;">
                <a href="${pageContext.request.contextPath}/search" style="cursor:zoom-in;" title="Search">
                    <span class="center">
                        <img alt="pokedex" src="${pageContext.request.contextPath}/images/pokedex.png">
                    </span>
                </a>
            </h1>

            <!-- Desktop Controls -->
            <div class="desktop-controls" style="display:flex;align-items:center;justify-content:center;flex-wrap:wrap;row-gap:10px;width:100%;margin:0 auto;">
                <div class="gif-toggle">
                    <label class="switch" title="If GIF is not present, official artwork will show!">
                        <input id="gifSwitch"
                               type="checkbox"
                               onclick="toggleGifs();"
                               ${showGifs ? 'checked' : ''}>

                        <span class="slider round"></span>
                    </label>

                    <label for="gifSwitch">
                        Show GIFs
                    </label>
                </div>
                &emsp;
                <div id="themeToggle" class="theme-toggle ${isDarkMode ? 'dark' : ''}">
                    <img src="images/sun.png" alt="Switch to dark mode" class="sun"
                         title="Switch to dark mode" onclick="toggleDarkmode(!document.body.classList.contains('darkmode'));">
                    <img src="images/moon.png" alt="Switch to light mode" class="moon"
                         title="Switch to light mode" onclick="toggleDarkmode(!document.body.classList.contains('darkmode'));">
                </div>
                &emsp;
                <div id="searchForPkmn" class="search-box" style="display:flex; --input-shadow-color:${tileColorParam};">
                    <input id="search" name="search" type="text" placeholder="Name or ID"/>
                    <div class="search-icon">
                        <img alt="pokéball" src="${pageContext.request.contextPath}/images/pokeball_search.png"
                             class="button cursor search-icon" title="Search for Pokemon" style="height:50px;width:50px;"
                             onclick="searchForPkmn(${isDarkMode})">
                    </div>
                </div>
                &emsp;
                <div id="jumpToPage" class="input-box" style="--input-shadow-color:${tileColorParam};">
                    <input id="pageNumber" name="pageNumber" type="text" placeholder="Page #"
                           style="color:${isDarkMode?'white':'black'};"
                           onclick="this.focus();"/>
                    <div class="input-icon" style="color:${isDarkMode?'white':'black'};"
                         onclick="setPageToView($('#pageNumber').val());" title="Jump to Page">
                        <i class="fa-brands fa-page4" style="font-size:28px; cursor:pointer;"></i>
                    </div>
                </div>
                &emsp;
                <div id="showPokemon" class="input-box" style="--input-shadow-color:${tileColorParam};">
                    <input id="showPkmnNumber" name="showPkmnNumber" type="text" placeholder="# of PkMn"
                           style="color:${isDarkMode?'white':'black'};"
                           onclick="this.focus();"/>
                    <div class="input-icon" style="color:${isDarkMode?'white':'black'};"
                         onclick="setPkmnPerPage();" title="Show Pok&#233;mon">
                        <i class="fa-solid fa-list-ol" style="font-size:28px; cursor:pointer;"></i>
                    </div>
                </div>
                &emsp;
                <button class="back-to-landing-btn icon" onclick="navigateToLandingPage()"
                        style="background-color:${tileColorParam};"
                        title="Return to Landing Page">
                    Back to Landing Page
                </button>
            </div>
        </div>
        <br>
        <jsp:include page="navigation.jsp"/>

        <div id="pokemonGrid" class="list-grid" style="display:grid;">
            <c:forEach items="${pokemonMap.entrySet()}" var="pokemon">
                <c:set var="pokemonId" value="${pokemon.value.id}" />
                <div id="pokemon${pokemonId}">
                    <a href="pokedex/${pokemon.value.id}">
                        <div id="pokemon${pokemonId}Box" class="box" title="Click for more info" style="background-color:${pokemon.value.color};">
                            <div id="nameAndId" style="display:inline-flex;">
                                <h5 id="name" style="color:black;">${pokemon.value.name.substring(0,1).toUpperCase()}${pokemon.value.name.substring(1)}</h5>
                                <div style="display: block;">&nbsp;&nbsp;&nbsp;&nbsp;</div>
                                <h5 id="id" style="color:black;">ID: ${pokemon.value.id}</h5>
                            </div>
                            <c:set var="defaultImagePresent" value="${pokemonSprites.get(pokemon.value.name)['defaultImagePresent']}" />
                            <c:set var="gifImagePresent" value="${pokemonSprites.get(pokemon.value.name)['gifImagePresent']}" />

                            <div id="image">
                                <c:choose>
                                    <c:when test="${defaultImagePresent}">
                                        <c:choose>
                                            <c:when test="${showGifs and gifImagePresent}">
                                                <img src="${pokemonSprites.get(pokemon.value.name)['gif']}" alt="${pokemon.value.name}-gif" style="height:200px;width:200px;">
                                            </c:when>
                                            <c:when test="${showGifs}">
                                                <c:choose>
                                                    <c:when test="${pokemonSprites.get(pokemon.value.name)['official']}">
                                                        <img src="${pokemonSprites.get(pokemon.value.name)['official']}" alt="${pokemon.value.name}-official" style="height:200px;width:200px;">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img src="${pageContext.request.contextPath}/images/pokeball_search.png" alt="no image found" style="height:200px;width:200px;">
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>
                                                <img onmouseover="showArtwork(this, '${pokemon.value.officialImage}')"
                                                     onmouseout="showArtwork(this, '${pokemon.value.defaultImage}');"
                                                     src="${pokemonSprites.get(pokemon.value.name)['front']}"
                                                     alt="${pokemon.value.name}-default"
                                                     style="height:200px;width:200px;">
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${not empty pokemonSprites.get(pokemon.value.name)['official']}">
                                                <img src="${pokemonSprites.get(pokemon.value.name)['official']}" alt="${pokemon.value.name}-official" style="height:200px;width:200px;">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${pageContext.request.contextPath}/images/pokeball_search.png" alt="no image found" style="height:200px;width:200px;">
                                            </c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                            <div id="info" style="display:inline-block;">
                                <h5 id="heightOfPokemon" style="color:black;">Height: ${pokemon.value.heightInInches} in</h5>
                                <h5 id="weightOfPokemon" style="color:black;">Weight: ${pokemon.value.weightInPounds} lb</h5>
                                <h5 id="colorOfPokemon" style="color:black;">Color: ${pokemon.value.capitalizedColor}</h5>
                                <h5 id="typeOfPokemon" style="color:black;">Type: ${pokemon.value.type}</h5>
                            </div>
                        </div>
                    </a>
                </div>
            </c:forEach>
        </div>

        <jsp:include page="navigation.jsp"/>

    </body>

    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.12.9/dist/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.0.0/dist/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>
    <script>
        function applyThemeToggleState(themeToggle, isDark) {
            if (!themeToggle) {
                return;
            }
            const sunIcon = themeToggle.querySelector(".sun");
            const moonIcon = themeToggle.querySelector(".moon");
            if (!sunIcon || !moonIcon) {
                return;
            }
            themeToggle.classList.toggle("dark", isDark);
            sunIcon.style.opacity = isDark ? "0" : "1";
            sunIcon.style.pointerEvents = isDark ? "none" : "auto";
            moonIcon.style.opacity = isDark ? "1" : "0";
            moonIcon.style.pointerEvents = isDark ? "auto" : "none";
        }

        function syncThemeTogglesWithBody() {
            const isDark = document.body.classList.contains("darkmode");
            applyThemeToggleState(document.getElementById("themeToggle"), isDark);
            applyThemeToggleState(document.getElementById("mobileThemeToggle"), isDark);
            $(".mobile-darkmode-label").text(isDark ? "Dark Mode" : "Light Mode");
        }

        $(function() {
            clearHomepageQueryParams();
            updateGifToggle(false, "${showGifs}");

            let ids = "${pokemonIds}".replace(/[\[\]]/g, '').split(',').map(id => id.trim());
            for(let i=0; i<ids.length; i++) {
                let pokemonBox = document.getElementById("pokemon"+(ids[i])+"Box");
                let currentColor = pokemonBox.style.backgroundColor;
                pokemonBox.style.backgroundColor = changeColor(currentColor);
            }

            let element = $(".page-link").filter(function() {
                return $(this).html() === '${page}';
            });
            if (element !== undefined) {
                element.addClass("active");
            }
            $('#search').on('keypress', function(e) {
                if ($('#search').val() === '') return;
                if (e.code === 'Enter' || e.code === 'Return') {
                    searchForPkmn('${isDarkMode}');
                }
            });

            // mobile version in mobileMenu.jsp

            $('#pageNumber').on('keypress', function(e) {
                if ($('#pageNumber').val() === '') return;
                if (e.code === 'Enter' || e.code === 'Return') {
                    setPageToView($('#pageNumber').val());
                }
            });

            $('#showPkmnNumber').on('keypress', function(e) {
                if ($('#showPkmnNumber').val() === '') return;
                if (e.code === 'Enter' || e.code === 'Return') {
                    setPkmnPerPage();
                }
            });

            $('#pageNumberMobile').on('keypress', function(e) {
                if ($('#pageNumberMobile').val() === '') return;
                if (e.code === 'Enter' || e.code === 'Return') {
                    setPageToView($('#pageNumberMobile').val());
                }
            });

            $('#showPkmnNumberMobile').on('keypress', function(e) {
                if ($('#showPkmnNumberMobile').val() === '') return;
                if (e.code === 'Enter' || e.code === 'Return') {
                    setPkmnPerPageMobile();
                }
            });
            syncThemeTogglesWithBody();
            window.addEventListener("resize", syncThemeTogglesWithBody);
            window.addEventListener("orientationchange", syncThemeTogglesWithBody);
        });

        function clearHomepageQueryParams() {
            const currentUrl = new URL(window.location.href);
            if (!currentUrl.searchParams.has('darkmode') && !currentUrl.searchParams.has('tileColor')) {
                return;
            }

            const cleanUrl = currentUrl.pathname + currentUrl.hash;
            window.history.replaceState({}, document.title, cleanUrl);
        }

        // mobile menu functions

        function setPkmnPerPage() {
            let value = $("#showPkmnNumber").val();
            if (value === '') return;
            setPkmnPerPageImpl(value, false);
        }

        function setPkmnPerPageImpl(value, isMobile) {
            console.log("showPkmnNumber: " + value);
            $.ajax({
                type: "GET",
                url: "pkmnPerPage",
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

        function toggleGifs() {
            $.ajax({
                type: "GET",
                url: "toggleGifs",
                async: false,
                dataType: "application/json",
                crossDomain: true,
                statusCode: {
                    200: function(data) {
                        updateGifToggle(true, data);
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
                this.closeMobileMenu();
            }, 500);
        }

        function toggleDarkmode(updatedDarkmode) {
            console.log('toggling darkmode: ' + updatedDarkmode);
            const isDark = updatedDarkmode === true || updatedDarkmode === "true";
            const themeToggle = document.getElementById("themeToggle");
            const sunIcon = themeToggle.querySelector(".sun");
            const moonIcon = themeToggle.querySelector(".moon");
            const outgoingIcon = isDark ? sunIcon : moonIcon;
            const incomingIcon = isDark ? moonIcon : sunIcon;
            const rotation = isDark ? 180 : -180;

            outgoingIcon.animate([
                { opacity: 1, transform: "rotate(0deg) scale(1)" },
                { opacity: 0, transform: "rotate(" + rotation + "deg) scale(0.5)" }
            ], { duration: 500, easing: "ease", fill: "forwards" });
            incomingIcon.animate([
                { opacity: 0, transform: "rotate(" + (-rotation) + "deg) scale(0.5)" },
                { opacity: 1, transform: "rotate(0deg) scale(1)" }
            ], { duration: 500, easing: "ease", fill: "forwards" });

            themeToggle.classList.toggle("dark", isDark);
            sunIcon.style.opacity = isDark ? "0" : "1";
            sunIcon.style.pointerEvents = isDark ? "none" : "auto";
            moonIcon.style.opacity = isDark ? "1" : "0";
            moonIcon.style.pointerEvents = isDark ? "auto" : "none";

            $.ajax({
                type: "GET",
                url: "toggleDarkmode",
                data: {
                    darkmode: updatedDarkmode
                },
                async: false,
                dataType: "application/json",
                crossDomain: true,
                statusCode: {
                    200: function(result) {
                        console.log('toggleDarkmode: ' + JSON.stringify(result.responseText));
                        const $body = $('body');
                        $body.toggleClass('dark darkmode', isDark);
                        $body.toggleClass('light lightmode', !isDark);
                        syncThemeTogglesWithBody();
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
            $("#gifSwitch").attr("checked", showGifs === 'true');
            $("#gifSwitchMobile").attr("checked", showGifs === 'true');
            if (reload) {
                setTimeout(function() {
                    location.reload();
                }, 500);
            }
        }

        function changeColor(pokemonColor) {
            console.log('changeColor: ' + pokemonColor);
            if (pokemonColor === "red") { return "#FA8072"; }
            else if (pokemonColor === "yellow") { return "#ffeb18"; }
            else if (pokemonColor === "green") { return "#AFE1AF"; }
            else if (pokemonColor === "blue") { return "#ADD8E6"; }
            else if (pokemonColor === "purple") { return "#CBC3E3"; }
            else if (pokemonColor === "brown") { return "#D27D2D"; }
            else if (pokemonColor === "white") { return "#d2cbd3"; }
            else if (pokemonColor === "pink") { return "#ef6bb6ff"; }
            else if (pokemonColor === "black") { return "#8f8b8b"}
            else if (pokemonColor === "gray" || pokemonColor === "grey") { return "#8f8b8b"}
            else return "#ffffff";
        }

        function showArtwork(imgTag, artwork) {
            imgTag.src = artwork;
        }

        function searchForPkmn(isDarkMode) {
            let nameOrId = $("#search").val().trim();
            if (nameOrId === '') {
                nameOrId = $("#searchMobile").val().trim();
                if (nameOrId === '') {
                    // if on mobile, rotated screen, you would get alert message just for opening the search.
                    // simply return to let it open.
                    return;
                }
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

        function setPageToView(pageNumber) {
            let value = pageNumber;
            if (pageNumber === '') return;
            console.log("page to view: " + value);
            $.ajax({
                type: "GET",
                url: "page",
                data: {
                    pageNumber: value
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
        
        function showLoadingOverlay(selectedType) {
            const overlay = document.getElementById('loadingOverlay');
            if (overlay) {
                overlay.style.display = 'flex';
                // update heading text with selectedType
                const heading = $("#loadingHeader");
                if (heading) {
                    heading.html('Fetching all ' + selectedType + ' Pok&#233mon');
                }

            }
        }
        
        function hideLoadingOverlay() {
            const overlay = document.getElementById('loadingOverlay');
            if (overlay) {
                overlay.style.display = 'none';
            }
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

</html>