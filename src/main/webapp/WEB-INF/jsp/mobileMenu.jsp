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
        <label id="mobileGifToggleLabel">${showGifs ? 'Hide GIFs' : 'Show GIFs'}</label>
        <div id="showGifsMobile" style="display:flex;align-items:center;justify-content:center;width:50px;height:50px;flex:0 0 50px;position:relative;overflow:hidden;">
            <img id="gifPreviewToggleMobile"
                 src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/25.png"
                 data-paused-src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/25.png"
                 data-animated-src="https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/animated/25.gif"
                 data-playing="${showGifs ? 'true' : 'false'}"
                 data-paused-scale="1.95"
                 alt="pikachu gif preview"
                 title="If GIF is not present, official artwork will show! Click to toggle GIFs."
                 style="cursor:pointer;width:50px;height:50px;object-fit:contain;position:absolute;top:50%;left:50%;display:block;"
                 onclick="handleGifPreviewClick();">
        </div>
    </div>

    <div class="mobile-menu-item mobile-gif-item">
        <label class="mobile-darkmode-label">${isDarkMode ? 'Dark Mode' : 'Light Mode'}</label>
        <div id="mobileThemeToggle" class="theme-toggle ${isDarkMode ? 'dark' : ''}">
            <img src="${pageContext.request.contextPath}/images/sun.png" alt="Switch to dark mode" class="sun"
                 title="Switch to dark mode"
                 onclick="toggleDarkmode();">
            <img src="${pageContext.request.contextPath}/images/moon.png" alt="Switch to light mode" class="moon"
                 title="Switch to light mode"
                 onclick="toggleDarkmode();">
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
                 onclick="setPageToView();"
                 title="Jump to Page">
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
    $(function(){
        logFromWhere('mobileMenu');
        //checks whether the pressed key is "Enter"
        $('#searchMobile').on('keypress', function(e) {
            if ($('#searchMobile').val() === '') return;
            if (e.code === 'Enter' || e.code === 'Return') {
                searchForPkmn();
            }
        });
        syncMobileGifPreviewState('${showGifs}' === 'true');
        syncMobileHeaderThemeFromBody();
    });
</script>