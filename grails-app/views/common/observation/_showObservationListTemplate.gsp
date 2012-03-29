
<div class="btn-group button-bar" data-toggle="buttons-radio" style="float:right;">
    <button id="list_view_bttn" class="btn list_style_button active"><i class=" icon-th-list"></i></button> 
    <button id="grid_view_bttn" class="btn grid_style_button"><i class="icon-th"></i></button>
</div>    
<div class="observations_list" class="observation grid_11" style="clear:both;">
    <div class="mainContent">
        <ul class="grid_view thumbnails">
            <g:each in="${observationInstanceList}" status="i"
                    var="observationInstance">
                    
                    <g:if test="${i%3 == 0}"> 
                        <li class="thumbnail" style="clear:both;">
                    </g:if>
                    <g:else>
                        <li class="thumbnail">
                    </g:else>
                        <obv:showSnippetTablet
                                model="['observationInstance':observationInstance]"></obv:showSnippetTablet>
                    </li>        

            </g:each>
        </ul> 
        <ul class="list_view thumbnails" style="display:none;">
            <g:each in="${observationInstanceList}" status="i"
            var="observationInstance">

            <li class="thumbnail" style="clear:both;">
                <obv:showSnippet
                    model="['observationInstance':observationInstance]"></obv:showSnippet>
            </li>        
            </g:each>
        </ul>       
    </div>
    <g:if test="${observationInstanceTotal > queryParams.max}">
        <div class="btn loadMore">
            <span class="progress" style="display:none;">Loading ... </span>
            <span class="buttonTitle">Load more</span>
        </div>
    </g:if>
    <div class="paginateButtons" style="visibility:hidden; clear: both">
        <g:paginate total="${observationInstanceTotal}" max="2" params="${activeFilters}"/>
    </div>
         <script>
                		
                $('#list_view_bttn').click(function(){
			$('.grid_view').hide();
			$('.list_view').fadeIn('slow');
			$(this).addClass('active');
                        //alert($(this).attr('class'));
			$('#grid_view_bttn').removeClass('active');
			$.cookie("observation_listing", "list");
		});
		
		$('#grid_view_bttn').click(function(){
			$('.grid_view').fadeIn('slow');
			$('.list_view').hide();
                        //alert($(this).attr('class'));
			$(this).addClass('active');
			$('#list_view_bttn').removeClass('active');
			$.cookie("observation_listing", "grid");
		});
	
                function eatCookies(){
                    if ($.cookie("observation_listing") == "list") {
                            $('.list_view').show();
                            $('.grid_view').hide();
                            $('#grid_view_bttn').removeClass('active');
                            $('#list_view_bttn').addClass('active');
                    }else{
                            $('.grid_view').show();
                            $('.list_view').hide();
                            $('#grid_view_bttn').addClass('active');
                            $('#list_view_bttn').removeClass('active');	
                    }
                }

                //eatCookies();

                $.autopager({
                 
                    autoLoad: false,
    		    // a selector that matches a element of next page link
    		    link: 'div.paginateButtons a.nextLink',

    		    // a selector that matches page contents
    		    content: '.mainContent',
                    
                    insertBefore: '.loadMore',
    		
    		    // a callback function to be triggered when loading start 
		    start: function(current, next) {
                        $(".loadMore .progress").show();
                        $(".loadMore .buttonTitle").hide();
		    },
		
		    // a function to be executed when next page was loaded. 
		    // "this" points to the element of loaded content.
		    load: function(current, next) {
		    			$(".mainContent:last").hide().fadeIn(3000);
                        if (next.url == undefined){
                            $(".loadMore").hide();
                        }else{
                            $(".loadMore .progress").hide();
                            $(".loadMore .buttonTitle").show();
                        }
		    }
		});
	
                $('.loadMore').click(function() {
                    $.autopager('load');
                    return false;
                });

        </script>

</div>
