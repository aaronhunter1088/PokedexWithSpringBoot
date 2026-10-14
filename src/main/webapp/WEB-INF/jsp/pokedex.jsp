<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Pok&#233;mon Info</title>
    <jsp:include page="headCommon.jsp"/>
    <style>
        /* Style the tab */
        .tab {
            overflow: hidden;
            border: 1px solid #ccc;
            background-color: #f1f1f1;
        }
        /* Style the buttons that are used to open the tab content */
        .tab button {
            background-color: inherit;
            float: left;
            border: none;
            outline: none;
            cursor: pointer;
            padding: 14px 16px;
            transition: 0.3s;
        }
        .parent-div {
            display: flex;
            justify-content: center;
            align-items: center;
            width: 100%;
            position: relative;
        }
        .left-div {
            padding-left: 15px;
            position: absolute;
            left: 0;
        }
        .center-div {
            text-align: center;
        }
    </style>
</head>
<body style="justify-content:space-evenly;text-align:center;"
      class="${isDarkMode?'darkmode':'lightmode'}">

    <jsp:include page="mobileMenu.jsp"/>
    
    <!-- Desktop Header -->
    <h1 id="pokedexSearchImgSearchLink" style="vertical-align:middle;">
        <a href="${pageContext.request.contextPath}/search" style="cursor:zoom-in;" title="Search">
            <span class="center">
            <img alt="pokedex" src="${pageContext.request.contextPath}/images/pokedex.png" style="width:100%;"></span>
        </a>
    </h1>
    <div id="pokedex">
        <div class="pokedex-grid">
            <div id="pokemonInfo" class="box" style="padding-top:10px;background-color:${pokemon.color};">
                <!-- Back arrow, Name, and ID -->
                <div id="backAndName" class="parent-div">
                    <div id="backArrow" class="left-div">
                        <a href="${pageContext.request.contextPath}/"><i class="fas fa-arrow-left fa-2x"></i></a>
                    </div>
                    <div id="nameAndID" class="center-div" style="display:inline-flex;gap:20px;">
                        <h3 id="name">Name: ${pokemon.name.substring(0,1).toUpperCase()}${pokemon.name.substring(1)}</h3>
                        <h3 id="idOfPokemon">ID: ${pokemon.id}</h3>
                    </div>
                </div>
                <br>
                
                <!-- Pokemon Image and Basic Info -->
                <div id="imageAndBasicInfo" style="display:flex;justify-content:center;align-items:center;gap:20px;flex-wrap:wrap;">
                    <div id="imageOfPokemon" style="justify-content:center;display:block;">
                        <img id="pkmnImage" src="${defaultImage}" alt="image" style="width:250px;height:250px;">
                    </div>
                    <div id="basicInfo" style="text-align:left;">
                        <h3 id="heightOfPokemon">Height: ${pokemon.heightInInches} in</h3>
                        <h3 id="weightOfPokemon">Weight: ${pokemon.weightInPounds} lb</h3>
                        <h3 id="colorOfPokemon">Color: ${pokemon.capitalizedColor}</h3>
                        <h3 id="typeOfPokemon">Type: ${pokemon.type}</h3>
                    </div>
                </div>
                <br>
                
                <!-- Action Buttons - Two Rows -->
                <div id="actionButtons" style="justify-content:center;text-align:center;">
                    <div id="infoButtonsDiv" style="display:flex;justify-content:center;gap:5px;flex-wrap:wrap;margin-bottom:10px;">
                        <button id="descriptionBtn" class="tab" onclick="showDiv('description', '${pokemon.id}');">Description</button>
                        <button id="locationsBtn" class="tab" onclick="showDiv('locations', '${pokemon.id}');">Locations</button>
                        <button id="movesBtn" class="tab" onclick="showDiv('moves', '${pokemon.id}');">Moves</button>
                        <button id="evolvesHowBtn" class="tab" onclick="showDiv('evolvesHow', '${pokemon.id}');">How ${pokemon.name().substring(0,1).toUpperCase()}${pokemon.name().substring(1)} Evolves</button>
                    </div>
                    <div id="photoButtonsDiv" style="display:flex;justify-content:center;gap:5px;flex-wrap:wrap;">
                        <button id="defaultImgBtn" class="tab" onclick="showImage('default', '${defaultImage}');">Show Default</button>
                        <button id="officialImgBtn" class="tab" onclick="showImage('official', '${officialImage}');">Show Official</button>
                        <button id="shinyImgBtn" class="tab" onclick="showImage('shiny', '${shinyImage}');">Show Shiny</button>
                        <button id="gifImgBtn" class="tab" onclick="showImage('gif', '${gifImage}');">Show Gif</button>
                    </div>
                </div>
                <br>
                
                <!-- Info Display Divs -->
                <div id="descriptionDiv" class="info-display-div">
                    <h3 id="description">${pokemon.description}</h3>
                </div>
                <div id="locationsDiv" style="overflow-y:scroll;overflow-x:hidden;height:200px;">
                    <c:choose>
                        <c:when test="${pokemon.locations.size() == 0}">
                            <h3 id="locations" class="listStyle">No known locations</h3>
                        </c:when>
                        <c:when test="${pokemon.locations.size() > 0}">
                            <c:forEach var="location" items="${pokemon.locations}">
                                <h3 id="locations" class="listStyle">${location}</h3>
                            </c:forEach>
                        </c:when>
                    </c:choose>
                </div>
                <div id="movesDiv" style="overflow-y:scroll;overflow-x:hidden;height:200px;">
                <c:forEach var="moveName" items="${pokemon.pokemonMoveNames}">
                    <h3 id="moves" class="listStyle">${moveName}</h3>
                </c:forEach>
                </div>
                <div id="evolvesHowDiv" class="info-display-div">
                    <jsp:include page="evolves-how.jsp" />
                </div>
            </div>
        </div>
    </div>

    <div id="evolutions"></div>

</body>

<c:set var="pokeballImg" value="/images/pokeball_search.png"/>
<script src="https://cdn.jsdelivr.net/npm/popper.js@1.12.9/dist/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.0.0/dist/js/bootstrap.min.js" integrity="sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>
<script type="text/javascript">
    $(function() {
        $("#defaultImgBtn").css('font-weight', 'bold');

        let pokemonInfoDiv = document.getElementById("pokemonInfo");
        pokemonInfoDiv.style.backgroundColor = changeColor("${pokemon.color}");

        defaultInfoDivs();
        $("#descriptionDiv").show();
        $("#descriptionBtn").css('font-weight', 'bold');
        if (Number.parseInt('${pokemon.id}') !== 0) {
            setEvolutionsDiv('${pokemon.id}');
            updateEvolutionsDiv();
        }
    });
</script>

</html>
