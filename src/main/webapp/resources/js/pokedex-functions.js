// Place common functions here

function logFromWhere(pageName) {
    console.log("Test function called from " + pageName);

}

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

function syncGifPreviewState(showGifsEnabled) {
    const gifPreviewToggle = document.getElementById("gifPreviewToggle");
    if (!gifPreviewToggle) {
        return;
    }
    gifPreviewToggle.src = showGifsEnabled
        ? gifPreviewToggle.dataset.animatedSrc
        : gifPreviewToggle.dataset.pausedSrc;
    gifPreviewToggle.dataset.playing = showGifsEnabled.toString();
    gifPreviewToggle.style.transform = showGifsEnabled
        ? "translate(-50%, -50%) scale(1)"
        : "translate(-50%, -50%) scale(" + (gifPreviewToggle.dataset.pausedScale || "1") + ")";
}

function applyGifModeToGrid(showGifsEnabled) {
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

        if (showGifsEnabled) {
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

function handleGifPreviewClick() {
    toggleGifs();
}

function clearHomepageQueryParams() {
    const currentUrl = new URL(window.location.href);
    if (!currentUrl.searchParams.has('darkmode') && !currentUrl.searchParams.has('tileColor')) {
        return;
    }

    const cleanUrl = currentUrl.pathname + currentUrl.hash;
    window.history.replaceState({}, document.title, cleanUrl);
}

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
        url: window.pokedexConfig.contextPath + "/toggleGifs",
        async: false,
        dataType: "application/json",
        crossDomain: true,
        statusCode: {
            200: function(data) {
                updateGifToggle(data);
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
        data: {
            darkmode: isDarkMode
        },
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

function updateGifToggle(data) {
    let showGifs;
    try {
        showGifs = JSON.parse(data.responseText);
    } catch (error) {
        showGifs = data;
    }
    const showGifsEnabled = showGifs === true || showGifs === "true";
    console.log("showGifs: " + showGifsEnabled);
    $("#gifSwitch").prop("checked", showGifsEnabled);
    $("#gifSwitchMobile").prop("checked", showGifsEnabled);
    syncGifPreviewState(showGifsEnabled);
    syncMobileGifPreviewState(showGifsEnabled);
    applyGifModeToGrid(showGifsEnabled);
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
        url: contextPath + "/pokemon/" + encodedNameOrId + "?darkmode=" + isDarkMode,
        dataType: "json"
    })
    .done(function() {
        const url = contextPath + "/pokedex/" + encodedNameOrId
            + "?darkmode=" + isDarkMode;

        console.log("Navigating to: " + url);
        window.location.href = url;
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

function setPageToView(pageNumber) {
    let value = pageNumber;
    if (pageNumber === '') return;
    console.log("page to view: " + value);
    //const contextPath = "${pageContext.request.contextPath}";
    window.location.href = window.pokedexConfig.contextPath + "/page?pageNumber=" + encodeURIComponent(value);
}

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
    url = window.pokedexConfig.env !== "production" && isMobile
        ? "http://" + window.location.hostname + ":4200?tileNumber=1&darkmode=" + currentDarkMode
        : "http://localhost:4200?tileNumber=1&darkmode=" + currentDarkMode;
    url = window.pokedexConfig.env === "production"
        ? "https://mypokedex.us?tileNumber=1&darkmode=" + currentDarkMode
        : url;
    window.location.href = url;
}

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

//let pokemon = {}, sprites = {};
//let locations = [], moves = [];
//let desc = "", color = "";
// Actions performed when you click "Get Info"
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
// Ajax to get Pokemon
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
// Ajax to get Moves
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
// Ajax to get Locations
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
// Ajax to get Description text
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
// Flips between regular and shiny images
function showImage(type) {
    let photoEle = $('#imageOfPokemon');
    let image;
    switch (type) {
        case 'default' : {
            image = sprites["frontDefault"];
            if (image === null) {
                image = "/images/pokeball_search.png";
            }
            $('#pokemonDefaultImgBtn').css('font-weight', 'bold');
            $('#pokemonShinyImgBtn').css('font-weight', 'normal');
            break;
        }
        case 'shiny' : {
            image = sprites["frontShiny"];
            if (image === null) {
                image = "/images/pokeball_search.png";
            }
            $('#pokemonDefaultImgBtn').css('font-weight', 'normal');
            $('#pokemonShinyImgBtn').css('font-weight', 'bold');
            break;
        }
        default : {
            image = "/images/pokeball_search.png";
            break;
        }
    }
    photoEle.prop("src", image);
    photoEle.show();
}
// Ajax to get the color of the Pokemon
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
// Sets the text fields
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
// Determines if the input is valid or not
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
// Shows the div based on the tab click
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
// Default setting for button weights
function setButtonWeightDefaults()  {
    $('#descTabBtn').css('font-weight', 'bold');
    $('#locTabBtn').css('font-weight', 'normal');
    $('#movesTabBtn').css('font-weight', 'normal');
    //$('#berriesTabBtn').css('font-weight', 'normal');
}
// Default setting for tab visuals
function setTabDivDefaults() {
    $('#descriptionDiv').show();
    $('#locationsDiv').hide();
    $('#movesDiv').hide();
    //$('#berriesDiv').hide();
}

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

function updateEvolutionsDiv() {
    let pokemonDivs = $("#evolutions div div a div.box");
    pokemonDivs.each((index, div) => {
        div.style.backgroundColor = changeColor(div.style.backgroundColor);
        console.log('updated color of ' + div.id);
    });
}

// function navigateToHomePage() {
//     window.location.href = '${pageContext.request.contextPath}/';
// }

// Mobile functions

let mobileMenuCloseTimeoutId;
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

function syncMobileGifPreviewState(showGifsEnabled) {
    const gifPreviewToggle = document.getElementById("gifPreviewToggleMobile");
    const gifToggleLabel = document.getElementById("mobileGifToggleLabel");
    if (!gifPreviewToggle) {
        return;
    }
    gifPreviewToggle.src = showGifsEnabled
        ? gifPreviewToggle.dataset.animatedSrc
        : gifPreviewToggle.dataset.pausedSrc;
    gifPreviewToggle.dataset.playing = showGifsEnabled.toString();
    gifPreviewToggle.style.transform = showGifsEnabled
        ? "translate(-50%, -50%) scale(1)"
        : "translate(-50%, -50%) scale(" + (gifPreviewToggle.dataset.pausedScale || "1") + ")";
    if (gifToggleLabel) {
        gifToggleLabel.textContent = showGifsEnabled ? "Hide GIFs" : "Show GIFs";
    }
}

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