// Place common functions here
let pokemon = {}, sprites = {};
let locations = [], moves = [];
let desc = "", color = "";

/**
 * Logs a message indicating the page from which the function was called.
 * @param pageName the name of the page from which the function was called
 */
function logFromWhere(pageName) {
    console.log("Test function called from " + pageName);

}

/**
 * Applies the dark mode state to the theme toggle button
 * based on the given isDark parameter.
 * @param themeToggle the theme toggle button element
 * @param isDark true to apply dark mode, false for light mode
 */
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

/**
 * Synchronizes the state of the theme toggle buttons with the
 * current dark mode state of the body.
 */
function syncThemeTogglesWithBody() {
    const isDark = document.body.classList.contains("darkmode");
    applyThemeToggleState(document.getElementById("themeToggle"), isDark);
    applyThemeToggleState(document.getElementById("mobileThemeToggle"), isDark);
    $(".mobile-darkmode-label").text(isDark ? "Dark Mode" : "Light Mode");
}

/**
 * Applies the dark mode state to the body and other relevant
 * elements based on the given isDark parameter.
 * @param isDark true to apply dark mode, false for light mode
 */
function applyDarkmodeState(isDark) {
    const $body = $("body");
    $body.toggleClass("dark darkmode", isDark);
    $body.toggleClass("light lightmode", !isDark);
    document.documentElement.style.backgroundColor = isDark ? "black" : "white";
    document.body.style.backgroundColor = isDark ? "black" : "white";
    $(".pokemon-logo").toggleClass("darkmode", isDark).toggleClass("lightmode", !isDark);
    if (typeof syncMobileHeaderThemeFromBody === "function") {
        syncMobileHeaderThemeFromBody(isDark);
    }
    syncThemeTogglesWithBody();
}

// TBR
function toggleGifPlayback(imageElement) {
    if (!imageElement) {
        return;
    }
    const isPlaying = imageElement.dataset.playing === "true";
    const nextSrc = isPlaying
        ? imageElement.dataset.pausedSrc
        : imageElement.dataset.animatedSrc;
    imageElement.src = nextSrc;
    imageElement.dataset.playing = (!isPlaying).toString();
}

/**
 * Sets the GIF preview state for the given preview
 * element based on the showGifs parameter.
 * @param previewId the ID of the preview element
 * @param showGifs true to show GIFs, false to hide them
 */
function setGifPreviewState(previewId, showGifs) {
    const gifPreviewToggle = document.getElementById(previewId);
    if (!gifPreviewToggle) {
        return;
    }
    gifPreviewToggle.src = showGifs
        ? gifPreviewToggle.dataset.animatedSrc
        : gifPreviewToggle.dataset.pausedSrc;
    gifPreviewToggle.dataset.playing = showGifs;
    gifPreviewToggle.style.transform = showGifs
        ? "translate(-50%, -50%) scale(1)"
        : "translate(-50%, -50%) scale(" + (gifPreviewToggle.dataset.pausedScale || "1") + ")";
}

/**
 * Syncs every GIF-related UI element (desktop and mobile previews, mobile label,
 * and the Pokemon grid images) to the given state.
 * @param showGifs true to show GIFs, anything else to hide them
 */
function syncGifState(showGifs) {
    setGifPreviewState("gifPreviewToggle", showGifs);
    setGifPreviewState("gifPreviewToggleMobile", showGifs);
    const gifToggleLabel = document.getElementById("mobileGifToggleLabel");
    if (gifToggleLabel) {
        gifToggleLabel.textContent = showGifs ? "Hide GIFs" : "Show GIFs";
    }
    document.querySelectorAll(".pokemon-grid-image").forEach(function(imageElement) {
        const hasDefaultImage = imageElement.dataset.defaultImagePresent === "true";
        const hasGifImage = imageElement.dataset.gifImagePresent === "true";
        const frontSrc = imageElement.dataset.frontSrc || "";
        const gifSrc = imageElement.dataset.gifSrc || "";
        const officialSrc = imageElement.dataset.officialSrc || "";
        const fallbackSrc = imageElement.dataset.fallbackSrc || "";
        const hoverSrc = imageElement.dataset.hoverSrc || frontSrc;

        imageElement.onmouseover = null;
        imageElement.onmouseout = null;

        if (showGifs) {
            if (hasDefaultImage && hasGifImage && gifSrc) {
                imageElement.src = gifSrc;
            } else if (officialSrc) {
                imageElement.src = officialSrc;
            } else {
                imageElement.src = fallbackSrc;
            }
            return;
        }

        if (hasDefaultImage && frontSrc) {
            imageElement.src = frontSrc;
            imageElement.onmouseover = function() {
                showArtwork(this, hoverSrc);
            };
            imageElement.onmouseout = function() {
                showArtwork(this, frontSrc);
            };
            return;
        }

        imageElement.src = officialSrc || fallbackSrc;
    });
    closeMobileMenu();
}

/**
 * Clears the query parameters related to dark mode
 * and tile color from the homepage URL.
 */
function clearHomepageQueryParams() {
    const currentUrl = new URL(window.location.href);
    if (!currentUrl.searchParams.has('darkmode') && !currentUrl.searchParams.has('tileColor')) {
        return;
    }

    const cleanUrl = currentUrl.pathname + currentUrl.hash;
    window.history.replaceState({}, document.title, cleanUrl);
}

/**
 * Sets the number of Pokémon to display per page based on the value selected in the
 * dropdown menu with the ID "showPkmnNumber". If the value is not empty, it calls
 * setPkmnPerPageImpl with the selected value and false for the isMobile parameter.
 */
function setPkmnPerPage() {
    let value = $("#showPkmnNumber").val();
    if (value === '') return;
    setPkmnPerPageImpl(value, false);
}

/**
 * Sets the number of Pokémon to display per page and reloads the page.
 * It sends an AJAX request to the server with the specified value and
 * handles the response accordingly. If the request is successful, it
 * reloads the page and clears any query parameters related to dark mode
 * or tile color from the homepage URL.
 * @param value the number of Pokémon to display per page
 * @param isMobile true if the request is made from a mobile device, false otherwise
 */
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
                clearHomepageQueryParams();
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

/**
 * Toggles the GIF display state of the application. It sends an AJAX request to the
 * server to update the user's preference and then updates the UI accordingly. The
 * function also clears any query parameters related to dark mode or tile color from
 * the homepage URL.
 */
function toggleGifs() {
    $.ajax({
        type: "GET",
        url: window.pokedexConfig.contextPath + "/toggleGifs",
        async: false,
        dataType: "application/json",
        crossDomain: true,
        statusCode: {
            200: function(data) {
                updateGifToggle(data);
                clearHomepageQueryParams();
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
    }, 500);
}

/**
 * Toggles the dark mode state of the application. It updates the body's class list,
 * synchronizes the theme toggles, and sends an AJAX request to save the user's
 * preference on the server. The function also handles the animation of the sun and
 * moon icons in the theme toggle button.
 */
function toggleDarkmode() {
    let isDarkMode = document.body.classList.contains("darkmode");

    console.log("isDarkMode: " + isDarkMode);
    isDarkMode = !isDarkMode;
    const themeToggle = document.getElementById("themeToggle");
    applyDarkmodeState(isDarkMode);

    if (themeToggle) {
        const sunIcon = themeToggle.querySelector(".sun");
        const moonIcon = themeToggle.querySelector(".moon");
        if (sunIcon && moonIcon) {
            const outgoingIcon = isDarkMode ? sunIcon : moonIcon;
            const incomingIcon = isDarkMode ? moonIcon : sunIcon;
            const rotation = isDarkMode ? 180 : -180;

            outgoingIcon.animate([
                { opacity: 1, transform: "rotate(0deg) scale(1)" },
                { opacity: 0, transform: "rotate(" + rotation + "deg) scale(0.5)" }
            ], { duration: 500, easing: "ease", fill: "forwards" });
            incomingIcon.animate([
                { opacity: 0, transform: "rotate(" + (-rotation) + "deg) scale(0.5)" },
                { opacity: 1, transform: "rotate(0deg) scale(1)" }
            ], { duration: 500, easing: "ease", fill: "forwards" });
        }
    }

    $.ajax({
        type: "GET",
        url: window.pokedexConfig.contextPath + "/toggleDarkmode",
        // data: {
        //     darkmode: isDarkMode
        // },
        success: function() {
            console.log('dark mode preference saved: ' + isDarkMode);
        },
        error: function(xhr, status, error) {
            console.error('Failed to update dark mode: ' + status + ' - ' + error);
        }
    });
    setTimeout(() => {
        closeMobileMenu();
    }, 1000);
}

/**
 * Updates the GIF toggle state based on the provided data and synchronizes the UI accordingly.
 * @param data the response from /toggleGifs or from window.pokedexConfig.showGifs
 */
function updateGifToggle(data) {
    let showGifs;
    try {
        if (data.responseText)
            showGifs = JSON.parse(data.responseText);
        else
            showGifs = data;
    } catch (error) {
        showGifs = data;
    }
    console.log("showGifs: " + showGifs);
    $("#gifSwitch").prop("checked", showGifs);
    $("#gifSwitchMobile").prop("checked", showGifs);
    window.pokedexConfig.showGifs = showGifs;
    syncGifState(showGifs);
    if (showGifs === false && isBold("#gifImgBtn")) {
        $("#defaultImgBtn").trigger("click");
    } else if (showGifs === true && isBold("#defaultImgBtn")
            && $("#gifImgBtn").length && !$("#gifImgBtn").attr("onclick").includes("'gif', ''")) {
        $("#gifImgBtn").trigger("click");
    }
}

/**
 * Checks if the font weight of the element specified by the selector is bold.
 * @param selector A jQuery selector string for the element to check.
 * @returns {boolean} true if the font weight is bold, false otherwise.
 */
function isBold(selector) {
    const weight = $(selector).css('font-weight');
    return weight === 'bold' || Number.parseInt(weight) >= 700;
}

/**
 * Changes the background color based on the given Pokémon color.
 * @param pokemonColor The color of the Pokémon (e.g., "red", "yellow", "green", etc.).
 * @returns {string} the corresponding hex color code for the background.
 */
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

/**
 * Updates the source of the given image tag to display the specified artwork.
 * @param imgTag the image tag element to update
 * @param artwork the URL of the artwork to display
 */
function showArtwork(imgTag, artwork) {
    imgTag.src = artwork;
}

/**
 * Searches for a Pokémon by name or ID and navigates to its page if found.
 */
function searchForPkmn() {
    let nameOrId = ($("#search").val() || "").trim();

    if (nameOrId === "") {
        nameOrId = ($("#searchMobile").val() || "").trim();

        if (nameOrId === "") {
            // Avoid searching when opening the mobile search
            // after rotating the screen.
            return;
        }
    }

    console.log("nameOrId: " + nameOrId);

    const contextPath = window.pokedexConfig.contextPath;
    const encodedNameOrId = encodeURIComponent(nameOrId);
    const isDarkMode = document.body.classList.contains("darkmode");

    console.log("isDarkMode: " + isDarkMode);

    $.ajax({
        type: "GET",
        url: contextPath + "/pokemon/" + encodedNameOrId, // + "?darkmode=" + isDarkMode,
        dataType: "json"
    })
    .done(function() {
        const url = contextPath + "/pokedex/" + encodedNameOrId;
            //+ "?darkmode=" + isDarkMode;

        console.log("Navigating to: " + url);
        window.location.href = url;
        clearHomepageQueryParams();
    })
    .fail(function(xhr) {
        console.log(xhr.responseText);

        if (xhr.status === 400) {
            alert("Pokémon not found. Please check the Name or ID and try again.");
        } else {
            alert("Pokémon search failed. Please try again later.");
        }
    });
}

/**
 * Sets the page number to view and reloads the page with the specified page number.
 * @param pageNumber the page number to view
 */
function setPageToView(pageNumber) {
    //let value = pageNumber;
    if (pageNumber === '') return;
    console.log("page to view: " + pageNumber);
    const contextPath = window.pokedexConfig.contextPath;
    //const encodedNameOrId = encodeURIComponent(pageNumber);
    $.ajax({
        type: "GET",
        url: contextPath + "/page",
        dataType: "html",
        data: {
            pageNumber: pageNumber
        }
    }).done(function() {
            const url = window.pokedexConfig.contextPath + "/page?pageNumber=" + encodeURIComponent(pageNumber);
            console.log("Navigating to: " + url);
            window.location.reload();
            clearHomepageQueryParams();
        })
        .fail(function(xhr) {
            console.log(xhr.responseText);

            if (xhr.status === 400) {
                alert("Page not found.");
            } else {
                alert("Page search failed. Please try again later.");
            }
        });
    //const contextPath = "${pageContext.request.contextPath}";
    //window.location.href = window.pokedexConfig.contextPath + "/page?pageNumber=" + encodeURIComponent(value);
}

/**
 * To be implemented.
 * Searches for Pokémon by type and navigates to the homepage with the selected type.
 * @param selectObject the select element containing the chosen type
 */
function getByPkmnType(selectObject) {
    let type = selectObject.value;

    // Show loading overlay if a type is selected (not "none")
    if (type !== 'none') {
        showLoadingOverlay(type);
    }

    $.ajax({
        type: "GET",
        url: window.pokedexConfig.contextPath + "/getPokemonByType",
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
                window.location.href = window.pokedexConfig.contextPath + "/";
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

/**
 * To be implemented with getByPkmnType.
 * Displays a loading overlay with a message indicating that Pokémon of the selected type are being fetched.
 * @param selectedType the type of Pokémon being fetched
 */
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

/**
 * To be implemented with getByPkmnType.
 * Hides the loading overlay.
 */
function hideLoadingOverlay() {
    const overlay = document.getElementById('loadingOverlay');
    if (overlay) {
        overlay.style.display = 'none';
    }
}

/**
 * Navigates to the landing page of the application, preserving the current dark mode state in the URL parameters.
 */
function navigateToLandingPage() {
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    const currentDarkMode = document.body.classList.contains("darkmode");
    let url = "";
    url = window.pokedexConfig.env !== "production" && isMobile
        ? "http://" + window.location.hostname + ":4200?tileNumber=1&darkmode=" + currentDarkMode
        : "http://localhost:4200?tileNumber=1&darkmode=" + currentDarkMode;
    url = window.pokedexConfig.env === "production"
        ? "https://mypokedex.us?tileNumber=1&darkmode=" + currentDarkMode
        : url;
    window.location.href = url;
}

// TBR
function loadPokedexPageAjax(url) {
    $.ajax({
        type: "GET",
        url: window.pokedexConfig.contextPath + url,
        async: false,
        dataType: "application/json",
        statusCode: {
            200: function(data) {
                $("#pokedex").html(data.responseText);
                $("#pokedex").show();
                $("#pokedexSearchImgSearchLink").hide();
            },
            404: function() {
                console.log('Failed');
                $("#evolutionsDiv").html('Request failed');
            },
            500: function() {
                console.log('Server Error');
            }
        }
    });
}

// TBR
function getPokemonInfo() {
    let name = $('#nameOrId').val();
    name = name.toLowerCase().trim();
    if (!isNameValid(name)) { alert("Not a valid name or ID"); }
    else {
        pokemon = getPokemon(name);
        //console.log("pokemon set: " + JSON.stringify(pokemon));
        sprites = JSON.parse(pokemon)["sprites"];
        //console.log("sprites set: " + JSON.stringify(sprites));
        // // desc = getPokemonDescriptionText(name);
        // // locations = getLocations(name);
        // // moves = getMoves(name);
        // // color = setBackgroundAndGetColor(name);
        // // setupPokedex();
        // // showImage('default');
        // // $('#pokedex').show();
        // // setButtonWeightDefaults();
        // // setTabDivDefaults();
    }
}

// TBR. Not used. Referenced in getPokemonInfo().
function getPokemon(name) {
    pokemon = $.ajax({
        type: "GET",
        url: window.pokedexConfig.contextPath + "/pokemon/"+name,
        async: false,
        dataType: "application/json",
        success: function(data) {
            pokemon = JSON.parse(data.responseText);
        }
    });
    return pokemon.responseText;
}

// TBR
function getMoves() {
    moves = JSON.parse(pokemon)["moves"];
    //console.log(moves.length + " before moves text adding")
    let moveNames = [];
    moves.forEach(m => {
        let move = m['move'].name;
        //console.log(move);
        move = move[0].toUpperCase() + move.substring(1);
        moveNames.push(move);
    });
    moves = moveNames.sort();
    return moves;
}

// TBR
function getLocations(name) {
    $.ajax({
        type: "GET",
        url: window.pokedexConfig.contextPath + "/pokemon/locations/"+name,
        async: false,
        crossDomain: false,
        success: function(data) {
            locations = JSON.parse(data);
            //console.log("length: " + locations.length + " locations: " + locations);
            let locationText = [];
            for(let i=0; i<locations.length; i++) {
                let theName = locations[i];
                let newName = "";
                let names = theName.split("-");
                names.forEach(name => {
                    name = name[0].toUpperCase() + name.substring(1);
                    newName += name + " ";
                });
                locationText.push(newName.toString());
            }
            locations = locationText;
            //console.log("after: " + locations);
        }
    });
    return locations.sort();
}

// TBR
function getPokemonDescriptionText(name) {
    desc = $.ajax({
        headers: {
            accept: "application/json",
            contentType: "application/json"
        },
        type: "GET",
        url: window.pokedexConfig.contextPath + "/pokemon/"+name+"/description",
        async: false,
        dataType: "application/json",
        crossDomain: false,
        'success': function(data) {
            desc = JSON.parse(data.responseText);
        }
    });
    return desc.responseText;
}

/**
 * Updates the displayed Pokémon image based on the selected type and provided image URL.
 * @param type the type of image to display ('default', 'official', 'shiny', or 'gif')
 * @param image the URL of the image to display; if empty, a default Pokéball image will be used
 */
function showImage(type, image) {
    let photoEle = $("#pkmnImage");
    switch (type) {
        case 'default' : {
            if (image === '') {
                image = '/images/pokeball_search.png';
            }
            $("#defaultImgBtn").css('font-weight', 'bold');
            $("#officialImgBtn").css('font-weight', 'normal');
            $("#shinyImgBtn").css('font-weight', 'normal');
            $("#gifImgBtn").css('font-weight', 'normal');
            break;
        }
        case 'official' : {
            if (image === '') {
                image = '/images/pokeball_search.png';
            }
            $("#defaultImgBtn").css('font-weight', 'normal');
            $("#officialImgBtn").css('font-weight', 'bold');
            $("#shinyImgBtn").css('font-weight', 'normal');
            $("#gifImgBtn").css('font-weight', 'normal');
            break;
        }
        case 'shiny' : {
            if (image === '') {
                image = '/images/pokeball_search.png';
            }
            $("#defaultImgBtn").css('font-weight', 'normal');
            $("#officialImgBtn").css('font-weight', 'normal');
            $("#shinyImgBtn").css('font-weight', 'bold');
            $("#gifImgBtn").css('font-weight', 'normal');
            break;
        }
        case 'gif' : {
            if (image === '') {
                image = '/images/pokeball_search.png';
            }
            console.log('image: ' + image);
            $("#defaultImgBtn").css('font-weight', 'normal');
            $("#officialImgBtn").css('font-weight', 'normal');
            $("#shinyImgBtn").css('font-weight', 'normal');
            $("#gifImgBtn").css('font-weight', 'bold');
            break;
        }
        default : {
            image = '/images/pokeball_search.png';
            break;
        }
    }
    photoEle.prop("src", image);
    photoEle.show();
}

// TBR
function setBackgroundAndGetColor(name) {
    $.ajax({
        type: "GET",
        url: window.pokedexConfig.contextPath + "/pokemon/"+name+"/color",
        async: false,
        crossDomain: false,
        'success': function(data) {
            let colorRes = JSON.stringify(data).replaceAll("\"", "");
            //console.log("color before: " + colorRes);
            if (colorRes === "red") { document.body.style.backgroundColor = "#FA8072"; }
            else if (colorRes === "yellow") { document.body.style.backgroundColor  = "#FFC300"; }
            else if (colorRes === "green") { document.body.style.backgroundColor  = "#AFE1AF"; }
            else if (colorRes === "blue") { document.body.style.backgroundColor  = "#ADD8E6"; }
            else if (colorRes === "purple") { document.body.style.backgroundColor  = "#CBC3E3"; }
            else if (colorRes === "brown") { document.body.style.backgroundColor  = "#D27D2D"; }
            else if (colorRes === "white") { document.body.style.backgroundColor  = "#d3cbcb"; }
            else if (colorRes === "pink") { document.body.style.backgroundColor = colorRes; }
            else if (colorRes === "black") { document.body.style.backgroundColor = "#8f8b8b"}
            else if (colorRes === "gray" || colorRes === "grey") { document.body.style.backgroundColor = "#8f8b8b"}
            //console.log("colorRes: " + colorRes);
            color = colorRes;
        },
        'fail': function(data) {
            console.log('fail /pokemon/'+name+'/color: ' + data);
        }
    });
    //console.log("color after: " + color);
    return color;
}

// TBR
function setupPokedex() {
    pokemon = JSON.parse(pokemon);
    console.log("pkmn: " + pokemon);
    $('#idOfPokemon').text(pokemon.id);
    $('#nameOfPokemon').text(pokemon.name.charAt(0).toUpperCase() + pokemon.name.slice(1));
    $('#heightOfPokemon').text(pokemon.height);
    $('#weightOfPokemon').text(pokemon.weight);
    $('#colorOfPokemon').text(color[0].toUpperCase()+color.slice(1));
    let pokemonTypes = pokemon["types"];
    if (pokemonTypes.length > 1) {
        $('#typeOfPokemon').text(pokemonTypes[0].type.name[0].toUpperCase()+pokemonTypes[0].type.name.slice(1) +
            " and " +
            pokemonTypes[1].type.name[0].toUpperCase()+pokemonTypes[1].type.name.slice(1));
    }
    else {
        $('#typeOfPokemon').text(pokemonTypes[0].type.name[0].toUpperCase()+pokemonTypes[0].type.name.slice(1));
    }
    $('#description').text(desc);

    $('#locationsDiv').html("");
    if (locations.length === 0) {
        $('#locationsDiv').append("<h3 id=\"location\">No known locations!</h3>");
    }
    else {
        for(let i=0; i<locations.length; i++) {
            $('#locationsDiv').append("<h3 id=\"location"+i+"\">"+locations[i]+"</h3>");
        }
    }

    $('#movesDiv').html("");
    for(let i=0; i<moves.length; i++) {
        $('#movesDiv').append("<h3 id=\"moves"+i+"\">"+moves[i]+"</h3>");
    }
}

/**
 * TBR
 * Checks if the given Pokémon name is valid by making a synchronous AJAX request to the server.
 * @param name the name of the Pokémon to validate
 * @returns {boolean} true if the name is valid, false otherwise
 */
function isNameValid(name) {
    let result = false;
    $.ajax({
        type: "GET",
        url: "pokemon/"+name+"/validateName",
        async: false,
        dataType: "application/json",
        crossDomain: true,
        statusCode: {
            200: function() {
                result = true;
            },
            400: function() {
                result = false;
            }
        }
    });
    return result;
}

// TBR
function showDivAndBoldText(div) {
    if (div === "descriptionDiv") {
        $('#descriptionDiv').show();
        $('#locationsDiv').hide();
        $('#movesDiv').hide();
        $('#berriesDiv').hide();
        $('#descTabBtn').css('font-weight', 'bold');
        $('#locTabBtn').css('font-weight', 'normal')
        $('#movesTabBtn').css('font-weight', 'normal')
        $('#berriesTabBtn').css('font-weight', 'normal')
    }
    else if (div === "locationsDiv") {
        $('#descriptionDiv').hide();
        $('#locationsDiv').show();
        $('#movesDiv').hide();
        $('#berriesDiv').hide();
        $('#descTabBtn').css('font-weight', 'normal');
        $('#locTabBtn').css('font-weight', 'bold')
        $('#movesTabBtn').css('font-weight', 'normal')
        $('#berriesTabBtn').css('font-weight', 'normal')
    }
    else if (div === "movesDiv") {
        $('#descriptionDiv').hide();
        $('#locationsDiv').hide();
        $('#movesDiv').show();
        $('#berriesDiv').hide();
        $('#descTabBtn').css('font-weight', 'normal');
        $('#locTabBtn').css('font-weight', 'normal')
        $('#movesTabBtn').css('font-weight', 'bold')
        $('#berriesTabBtn').css('font-weight', 'normal')
    }
    // else if (div === "berriesDiv") {
    //     $('#descriptionDiv').hide();
    //     $('#locationsDiv').hide();
    //     $('#movesDiv').hide();
    //     $('#berriesDiv').show();
    //     $('#descTabBtn').css('font-weight', 'normal');
    //     $('#locTabBtn').css('font-weight', 'normal')
    //     $('#movesTabBtn').css('font-weight', 'normal')
    //     $('#berriesTabBtn').css('font-weight', 'bold')
    // }
}

// TBR
function setButtonWeightDefaults()  {
    $('#descTabBtn').css('font-weight', 'bold');
    $('#locTabBtn').css('font-weight', 'normal');
    $('#movesTabBtn').css('font-weight', 'normal');
    //$('#berriesTabBtn').css('font-weight', 'normal');
}

// TBR
function setTabDivDefaults() {
    $('#descriptionDiv').show();
    $('#locationsDiv').hide();
    $('#movesDiv').hide();
    //$('#berriesDiv').hide();
}

/**
 * Resets the visibility and font weight of all information divs
 * and their corresponding buttons to the default state.
 */
function defaultInfoDivs() {
    $("#descriptionDiv").hide();
    $("#descriptionBtn").css('font-weight', 'normal');
    $("#locationsDiv").hide();
    $("#locationsBtn").css('font-weight', 'normal');
    $("#movesDiv").hide();
    $("#movesBtn").css('font-weight', 'normal');
    $("#evolvesHowDiv").hide();
    $("#evolvesHowBtn").css('font-weight', 'normal');
}

/**
 * Shows the specified information div and sets the
 * corresponding button's font weight to bold.
 * @param divName the name of the div to show ('description', 'locations', 'moves', or 'evolvesHow')
 * @param pokemonId the ID of the Pokémon for which to fetch evolution information (used when divName is 'evolvesHow')
 */
function showDiv(divName, pokemonId) {
    defaultInfoDivs();
    switch (divName) {
        case 'description' : {
            $("#descriptionDiv").show();
            $("#descriptionDiv").css('display', 'inline-flex');
            $("#descriptionBtn").css('font-weight', 'bold');
            break;
        }
        case 'locations' : {
            $("#locationsDiv").show();
            $("#locationsBtn").css('font-weight', 'bold');
            break;
        }
        case 'moves' : {
            $("#movesDiv").show();
            $("#movesBtn").css('font-weight', 'bold');
            break;
        }
        case 'evolvesHow' : {
            $.ajax({
                type: "GET",
                url: window.pokedexConfig.contextPath + "/evolves-how",
                async: false,
                data: {
                    pokemonId: Number.parseInt(pokemonId)
                },
                dataType: "application/json",
                crossDomain: true,
                statusCode: {
                    200: function (data) {
                        $("#evolvesHowDiv").html(data.responseText);
                    },
                    404: function () {
                        console.log('Failed');
                        $("#evolutionsDiv").html('Request failed');
                    },
                    500: function () {
                        console.log('Server Error');
                    }
                }
            });
            $("#evolvesHowDiv").show();
            $("#evolvesHowBtn").css('font-weight', 'bold');
            break;
        }
        default : {
            console.log('Should never reach here!');
            $("descriptionDiv").show();
        }
    }
}

/**
 * Fetches and displays the evolution information for the specified Pokémon ID in the "evolutions" div.
 * @param pokemonId the ID of the Pokémon for which to fetch evolution information
 */
function setEvolutionsDiv(pokemonId) {
    let response = $.ajax({
        type: "GET",
        url: window.pokedexConfig.contextPath + "/evolutions/" + pokemonId,
        async: false,
        dataType: "json",
        crossDomain: true,
        success: function (data) {
            return data.responseText;
        },
        error: function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status === 404) {
                console.log('Failed');
                $("#evolutions").html('There was an error retrieving evolutions.'
                    + textStatus
                    + ': '
                    + errorThrown
                );
            } else if (jqXHR.status === 500) {
                console.log('Server Error');
            }
        }
    });
    if (response.responseText !== undefined) {
        /*
        response.responseText contains HTML as a string. jQuery's find() method searches
        within the DOM elements, not the outer container, so using filter() instead. When
        you do $(response.responseText), jQuery parses the HTML but the <body> tag itself
        becomes the root element, not a child element you can search for with find().
         */
        let firstH2Text = $(response.responseText).filter('h2').first().text().trim();
        //console.debug('firstH2Text: ' + firstH2Text);
        if (firstH2Text === 'No Evolutions') {
            $("#evolutions").hide();
        } else {
            $("#evolutions").html(response.responseText);
        }
    }
}

/**
 * Updates the background color of each Pokémon box in the "evolutions"
 * div based on its current background color. This function iterates
 * through each Pokémon box, retrieves its current background color,
 * and changes it using the changeColor function.
 * It also logs the ID of each updated Pokémon box to the console.
 */
function updateEvolutionsDiv() {
    let pokemonDivs = $("#evolutions div div a div.box");
    pokemonDivs.each((index, div) => {
        div.style.backgroundColor = changeColor(div.style.backgroundColor);
        console.log('updated color of ' + div.id);
    });
}

// Mobile functions

let mobileMenuCloseTimeoutId;

/**
 * Toggles the visibility of the mobile menu. If the menu is currently open,
 * it will be closed; if it is closed, it will be opened. The function also
 * manages the body's overflow style to prevent scrolling when the menu is
 * open.
 */
function toggleMobileMenu() {
    const menu = document.getElementById('mobileMenu');
    const overlay = document.getElementById('mobileMenuOverlay');
    const isOpen = menu.classList.contains('active'); // && !menu.classList.contains('closing');
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

/**
 * Closes the mobile menu if it is currently open. The function adds a "closing"
 * class to the menu and overlay elements, which can be used for CSS animations.
 * After a delay of 1 second (1000 milliseconds), the "active" and "closing"
 * classes are removed, and the body's overflow style is reset to allow scrolling
 * again.
 */
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
    }, 1000);
}

/**
 * Synchronizes the theme of the mobile header and menu with the current theme of the
 * body element. If the isDarkOverride parameter is provided, it will be used to
 * determine the theme; otherwise, the function will check the body's class list for
 * the "darkmode" class.
 * @param isDarkOverride
 */
function syncMobileHeaderThemeFromBody(isDarkOverride) {
    const isDark = typeof isDarkOverride === "boolean"
        ? isDarkOverride
        : document.body.classList.contains("darkmode");
    const $mobileHeader = $(".mobile-header");
    const $mobileMenu = $("#mobileMenu");
    document.documentElement.style.backgroundColor = isDark ? "black" : "white";
    document.body.style.backgroundColor = isDark ? "black" : "white";
    $mobileHeader
        .toggleClass("darkmode", isDark)
        .toggleClass("lightmode", !isDark)
        .css("background-color", isDark ? "black" : "white");
    $mobileMenu
        .toggleClass("darkmode", isDark)
        .toggleClass("lightmode", !isDark);
}