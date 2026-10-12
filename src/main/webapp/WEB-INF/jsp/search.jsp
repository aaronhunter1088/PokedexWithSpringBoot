<!DOCTYPE html>
<html lang="en">
<head>
    <title>Search</title>
    <jsp:include page="headCommon.jsp"/>
    <style></style>
</head>
<body style="justify-content:space-evenly;text-align:center;"
      class="${isDarkMode?'darkmode':'lightmode'}">
    <h1 id="searchImgReloadPage" style="vertical-align:middle;">
        <a href="${pageContext.request.contextPath}/search" style="cursor:zoom-in;" title="Search">
            <span class="center">
            <img alt="pokedex" src="${pageContext.request.contextPath}/images/pokedex.png" style="width:100%;"></span>
        </a>
    </h1>

    <h4 style="vertical-align: middle;">
        <a href="${pageContext.request.contextPath}/" title="Go back to list"><i class="fas fa-arrow-left" style="${isDarkMode?'color:white':'color:#000000'}"></i></a>
        <label for="search" style="display:none;"></label>
        <input id="search" name="nameOrId" placeholder="Name or ID" type="text" class="${isDarkMode?'darkmode':'lightmode'}"/>
        <img alt="Get Pokémon"
             src="${pageContext.request.contextPath}/images/pokeball_search.png"
             style="width:30px; height:30px; cursor: pointer;"
             title="Search for Pkmn"
             onclick="searchForPkmn();">
    </h4>

</body>

<script type="text/javascript">
    $(function(){
        logFromWhere('Search Page');
        $('#pokedex').hide();
        //checks whether the pressed key is "Enter"
        $('#search').on('keypress', function(e) {
            if ($('#search').val() === '') return;
            if (e.code === 'Enter' || e.code === 'Return') {
                searchForPkmn();
            }
        });
    });
</script>

</html>