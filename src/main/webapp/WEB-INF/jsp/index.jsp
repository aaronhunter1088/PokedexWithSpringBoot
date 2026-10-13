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
                transition: transform 2s ease;
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

            #desktopHeader {
                transition:
                    background-color 2s ease,
                    color 2s ease;
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
                <div id="showGifs" style="display:flex;align-items:center;justify-content:center;width:50px;height:50px;flex:0 0 50px;position:relative;overflow:hidden;">
                    <img id="gifPreviewToggle"
                         src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/25.png"
                         data-paused-src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/25.png"
                         data-animated-src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/animated/25.gif"
                         data-playing="false"
                         data-paused-scale="1.95"
                         alt="pikachu gif preview"
                         title="If GIF is not present, official artwork will show and if still nothing, a Pokeball."
                         style="cursor:pointer;width:50px;height:50px;object-fit:contain;position:absolute;top:50%;left:50%;display:block;"
                         onclick="handleGifPreviewClick();">
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
                    <input id="search" name="search" type="text" placeholder="Name or ID" autocomplete="off"/>
                    <div class="search-icon">
                        <img alt="pokéball" src="${pageContext.request.contextPath}/images/pokeball_search.png"
                             class="button cursor search-icon" title="Search for Pokemon" style="height:50px;width:50px;"
                             onclick="searchForPkmn()">
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
                            <c:set var="frontImage" value="${pokemonSprites.get(pokemon.value.name)['front']}" />
                            <c:set var="gifImage" value="${pokemonSprites.get(pokemon.value.name)['gif']}" />
                            <c:set var="officialImage" value="${pokemonSprites.get(pokemon.value.name)['official']}" />
                            <c:set var="fallbackImage" value="${pageContext.request.contextPath}/images/pokeball_search.png" />
                            <c:set var="hoverImage" value="${not empty officialImage ? officialImage : frontImage}" />
                            <c:set var="initialImageSrc" value="${fallbackImage}" />
                            <c:choose>
                                <c:when test="${defaultImagePresent}">
                                    <c:choose>
                                        <c:when test="${showGifs and gifImagePresent}">
                                            <c:set var="initialImageSrc" value="${gifImage}" />
                                        </c:when>
                                        <c:when test="${showGifs}">
                                            <c:choose>
                                                <c:when test="${not empty officialImage}">
                                                    <c:set var="initialImageSrc" value="${officialImage}" />
                                                </c:when>
                                                <c:otherwise>
                                                    <c:set var="initialImageSrc" value="${fallbackImage}" />
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="initialImageSrc" value="${frontImage}" />
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <c:choose>
                                        <c:when test="${not empty officialImage}">
                                            <c:set var="initialImageSrc" value="${officialImage}" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="initialImageSrc" value="${fallbackImage}" />
                                        </c:otherwise>
                                    </c:choose>
                                </c:otherwise>
                            </c:choose>

                            <div id="image">
                                <img class="pokemon-grid-image"
                                     src="${initialImageSrc}"
                                     data-front-src="${frontImage}"
                                     data-gif-src="${gifImage}"
                                     data-official-src="${officialImage}"
                                     data-fallback-src="${fallbackImage}"
                                     data-hover-src="${hoverImage}"
                                     data-default-image-present="${defaultImagePresent}"
                                     data-gif-image-present="${gifImagePresent}"
                                     alt="${pokemon.value.name}-image"
                                     style="height:200px;width:200px;"
                                     <c:if test="${defaultImagePresent and not showGifs}">
                                         onmouseover="showArtwork(this, '${hoverImage}')"
                                         onmouseout="showArtwork(this, '${frontImage}');"
                                     </c:if>>
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
        $(function() {
            logFromWhere('index');
            clearHomepageQueryParams();
            updateGifToggle(false);

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
    </script>

</html>