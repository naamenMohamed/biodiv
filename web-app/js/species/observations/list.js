/**
 * 
 */

$(document).ready(function(){
	$('#selected_sort').tooltip({placement:'right'});
    $('button').tooltip();
    $('.dropdown-toggle').dropdown();
    	
    $('#speciesNameFilter').button();
    if(window.params.speciesName == 'Unknown'){
		$("#speciesNameFilterButton").addClass('active');
		$("#speciesNameAllButton").removeClass('active');
	}else{
		$("#speciesNameFilterButton").removeClass('active');
		$("#speciesNameAllButton").addClass('active');
    }
    
	$('#observationFlagFilter').button();
    if(window.params.isFlagged == 'true' ){
		$("#observationFlaggedButton").addClass('active');
		$("#observationWithNoFlagFilterButton").removeClass('active')
	}else{
		$("#observationFlaggedButton").removeClass('active');
		$("#observationWithNoFlagFilterButton").addClass('active')
	}

   
   $("#observationFlaggedButton").click(function() {
   		if($("#observationFlaggedButton").hasClass('active')){
   			return false;
   		}
		$("#observationFlagFilter").val('true')
		$("#observationWithNoFlagFilterButton").removeClass('active')
		$("#observationFlaggedButton").addClass('active')
		
		updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
	});
	
	$("#observationWithNoFlagFilterButton").click(function() {
   		if($("#observationWithNoFlagFilterButton").hasClass('active')){
   			return false;
   		}
		$("#observationFlagFilter").val('false')
		$("#observationFlaggedButton").removeClass('active')
		$("#observationWithNoFlagFilterButton").addClass('active')
		
		updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
	});
	
	$("#speciesNameAllButton").click(function() {
   		if($("#speciesNameAllButton").hasClass('active')){
   			return false;
   		}
		$("#speciesNameFilter").val('All')
		$("#speciesNameFilterButton").removeClass('active')
		$("#speciesNameAllButton").addClass('active')
		
		updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
	});
	
	$("#speciesNameFilterButton").click(function() {
		if($("#speciesNameFilterButton").hasClass('active')){
   			return false;
   		}
	    $("#speciesNameFilter").val('Unknown')
		$("#speciesNameFilterButton").addClass('active')
	    $("#speciesNameAllButton").removeClass('active')
			
		updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;
	});
	                
	$('#speciesGroupFilter button').click(function(){
		if($(this).hasClass('active')){
			return false;
		}
		$('#speciesGroupFilter button.active').removeClass('active');
    	$(this).addClass('active');
    	updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
    	return false;
 	});
        
 	$('#habitatFilter button').click(function(){
 		if($(this).hasClass('active')){
			return false;
		}
 		$('#habitatFilter button.active').removeClass('active');
    	$(this).addClass('active');
    	updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
    	return false;
 	});
        
   $('.sort_filter_label').click(function(){
   		var caret = '<span class="caret"></span>'
   		if(stringTrim(($(this).html())) == stringTrim($("#selected_sort").html().replace(caret, ''))){
   			$("#sortFilter").hide();
        	return false;
		}
		$('.sort_filter_label.active').removeClass('active');
		$(this).addClass('active');
        $("#selected_sort").html($(this).html() + caret);
        $("#sortFilter").hide();
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
        return false;   
   });

	$("#selected_sort").click(function(){
		$("#sortFilter").show();
	});
	
        
    $(".paginateButtons a").click(function() {
        updateGallery($(this).attr('href'), window.params.queryParamsMax, undefined, undefined, window.params.isGalleryUpdate);
        return false;
    });
    
    $("ul[name='tags']").tagit({select:true,  tagSource: window.params.tagsLink});
	$("li.tagit-choice").live('click', function(){
   		setActiveTag($(this).contents().first().text());
    	updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
    	return false;
    });
   
   $('#tc_tagcloud a').live('click', function(){
   		setActiveTag($(this).contents().first().text());
		updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
		return false;
   });
   
    $("#removeTagFilter").live('click', function(){
    	var oldActiveTag = $("li.tagit-choice.active");
		if(oldActiveTag){
			oldActiveTag.removeClass('active');
		}
		var oldActiveTag = $("#tc_tagcloud a.active");
		if(oldActiveTag){
			oldActiveTag.removeClass('active');
		}
		updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
    	return false;
    });
 
    $("#removeUserFilter").live('click', function(){
    	updateGallery(undefined, window.params.queryParamsMax, window.params.offset, true, window.params.isGalleryUpdate);
    	return false;
    });
    
    $("#removeQueryFilter").live('click', function(){
    	$( "#searchTextField" ).val('');
    	updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, window.params.isGalleryUpdate);
    	return false;
    });
   
   var tmpTarget =  window.location.pathname + window.location.search;
   setActiveTag($('<a href="'+ tmpTarget +'"></a>').url().param()["tag"]);
   
   $('.list_view_bttn').live('click', function() {
		$('.grid_view').hide();
		$('.list_view').show();
		$(this).addClass('active');
		$('.grid_view_bttn').removeClass('active');
		$.cookie("observation_listing", "list");
		adjustHeight();
	});

	$('.grid_view_bttn').live('click', function() {
		$('.grid_view').show();
		$('.list_view').hide();
		$(this).addClass('active');
		$('.list_view_bttn').removeClass('active');
		$.cookie("observation_listing", "grid");
	});

	$('.loadMore').live('click', function() {
		$.autopager({

			autoLoad : false,
			// a selector that matches a element of next page link
			link : 'div.paginateButtons a.nextLink',

			// a selector that matches page contents
			content : '.mainContent',

			appendTo : '.mainContentList',

			// a callback function to be triggered when loading start 
			start : function(current, next) {

				$(".loadMore .progress").show();
				$(".loadMore .buttonTitle").hide();
			},

			// a function to be executed when next page was loaded. 
			// "this" points to the element of loaded content.
			load : function(current, next) {
				$(".mainContent:last").hide().fadeIn(3000);
				
				$("div.paginateButtons a.nextLink").attr('href', next.url);
				if (next.url == undefined) {
					$(".loadMore").hide();
				} else {
					$(".loadMore .progress").hide();
					$(".loadMore .buttonTitle").show();
				}
				if ($('.grid_view_bttn.active')[0]) {
					$('.grid_view').show();
					$('.list_view').hide();
				} else {
					$('.grid_view').hide();
					$('.list_view').show();
				}
					
				var a = $('<a href="'+current.url+'"></a>');
			    var url = a.url();
			    var params = url.param();
			    delete params["append"]
			    delete params["loadMore"]
			    params['max'] = parseInt(params['offset'])+parseInt(params['max']);
			    params['offset'] = 0
			    var History = window.History;
			    History.pushState({state:1}, "Species Portal", '?'+decodeURIComponent($.param(params))); 
				eatCookies();
			}
		});

		$.autopager('load');
		return false;
	});
	
	eatCookies();
});

if (typeof String.prototype.startsWith != 'function') {
	  // see below for better implementation!
	  String.prototype.startsWith = function (str){
	    return this.indexOf(str) == 0;
	  };
	}

function eatCookies() {
	
	var hashString = window.location.hash.substring(1)
	if ($.cookie("observation_listing") == "list") {
		if(!hashString.startsWith('l')) {
			if(hashString.startsWith('g')) {
				window.location.hash = "l"+hashString.substring(1);
			} else if(hashString){
				window.location.hash = "l"+hashString;
			}
		}
		$('.list_view').show();
		$('.grid_view').hide();
		$('.grid_view_bttn').removeClass('active');
		$('.list_view_bttn').addClass('active');
	} else {
		if(!hashString.startsWith('g')) {
			if(hashString.startsWith('l')) {
				window.location.hash = "g"+hashString.substring(1);
			} else if(hashString){
				window.location.hash = "g"+hashString;
			}
		}
		$('.grid_view').show();
		$('.list_view').hide();
		$('.grid_view_bttn').addClass('active');
		$('.list_view_bttn').removeClass('active');
	}
	adjustHeight();
}

function stringTrim(s){
	return $.trim(s);
}

function getSelectedGroup() {
    var grp = ''; 
    $('#speciesGroupFilter button').each (function() {
            if($(this).hasClass('active')) {
                    grp += $(this).attr('value') + ',';
            }
    });

    grp = grp.replace(/\s*\,\s*$/,'');
    return grp;	
} 
    
function getSelectedHabitat() {
    var hbt = ''; 
    $('#habitatFilter button').each (function() {
            if($(this).hasClass('active')) {
                    hbt += $(this).attr('value') + ',';
            }
    });
    hbt = hbt.replace(/\s*\,\s*$/,'');
    return hbt;	
} 
    
function getSelectedSortBy() {
    var sortBy = ''; 
     $('.sort_filter_label').each (function() {
            if($(this).hasClass('active')) {
                sortBy += $(this).attr('value') + ',';
            }
    });

    sortBy = sortBy.replace(/\s*\,\s*$/,'');
    return sortBy;	
} 

function getSelectedFlag() {
    var flag = ''; 
	flag = $("#observationFlagFilter").attr('value');
	if(flag) {
    	flag = flag.replace(/\s*\,\s*$/,'');
    	return flag;
    }	
} 
    
function getSelectedSpeciesName() {
    var sName = ''; 
	sName = $("#speciesNameFilter").attr('value');
	if(sName) {
    	sName = sName.replace(/\s*\,\s*$/,'');
    	return sName;
    }	
} 
	
function getSelectedTag() {
	var tag = ''; 
	tag = $("li.tagit-choice.active").contents().first().text();
	if(!tag){
		tag = $("#tc_tagcloud a.active").contents().first().text();	
	}
	if(tag) {
    	tag = stringTrim(tag.replace(/\s*\,\s*$/,''));
    	return tag;
    }	
} 

function getFilterParameters(url, limit, offset, removeUser) {
    var params = url.param();
    var sortBy = getSelectedSortBy();
    if(sortBy) {
            params['sort'] = sortBy;
    }

    var sName = getSelectedSpeciesName();
    if(sName) {
            params['speciesName'] = sName;
    }
	
	var flag = getSelectedFlag();
    if(flag) {
            params['isFlagged'] = flag;
    }
    var grp = getSelectedGroup();
    if(grp) {
            params['sGroup'] = grp;
    }

    var habitat = getSelectedHabitat();
    if(habitat) {
            params['habitat'] = habitat;
    }

    if(limit != undefined) {
        params['max'] = limit.toString();
    } 

    if(offset != undefined) {
        params['offset'] = offset.toString();
    }
	
	var tag = getSelectedTag();
	if(tag){
		params['tag'] = tag;
	}else{
		// removing old tag from url
		if(params['tag'] != undefined){
			delete params['tag'];
		}
	}
	
	var query = $( "#searchTextField" ).val();
	if(query){
		params['query'] = query;
	}else{
		// removing old tag from url
		if(params['query'] != undefined){
			delete params['query'];
		}
	}
	
	if(removeUser){
		if(params['user'] != undefined){
			delete params['user'];
		}
	}
	
	return params;
}	

function setActiveTag(activeTag){
	if(activeTag != undefined){
				$('li.tagit-choice').each (function() {
					if(stringTrim($(this).contents().first().text()) == stringTrim(activeTag)) {
       				$(this).addClass('active');
       			}
       			else{
       				if($(this).hasClass('active')){
       					$(this).removeClass('active');
       				}
       			}
       		});
       		
       		$('#tc_tagcloud a').each(function() {
					if(stringTrim($(this).contents().first().text()) == stringTrim(activeTag)) {
       				$(this).addClass('active');
       			}else{
       				if($(this).hasClass('active')){
       					$(this).removeClass('active');
       				}
       			}
			});
			
 		}
}

function updateListPage(activeTag) {
		return function (data) {
			$('.observations_list').replaceWith(data.obvListHtml);
			$('#info-message').replaceWith(data.obvFilterMsgHtml);
			$('#tags_section').replaceWith(data.tagsHtml);
			$('.observation_location_wrapper').replaceWith(data.mapViewHtml);
			setActiveTag(activeTag);
			eatCookies();
	}
}

function updateGallery(target, limit, offset, removeUser, isGalleryUpdate) {
    if(target === undefined) {
            target = window.location.pathname + window.location.search;
    }
    
    var a = $('<a href="'+target+'"></a>');
    var url = a.url();
    var href = url.attr('path');
    var params = getFilterParameters(url, limit, offset, removeUser);
    // alert(" tag in params " + params['tag'] );
    isGalleryUpdate = (isGalleryUpdate == undefined)?true:isGalleryUpdate
    if(isGalleryUpdate)
    	params["isGalleryUpdate"] = isGalleryUpdate;
    var recursiveDecoded = decodeURIComponent($.param(params));
    
    var doc_url = href+'?'+recursiveDecoded;
    var History = window.History;
    delete params["isGalleryUpdate"]
    History.pushState({state:1}, "Species Portal", '?'+decodeURIComponent($.param(params))); 
    // alert("doc_url " + doc_url);
    if(isGalleryUpdate) {
       	$.ajax({
				url: doc_url,
				dataType: 'json',
				
				beforeSend : function(){
					$('.observations_list').css({"opacity": 0.5});
					$('#tags_section').css({"opacity": 0.5});
				},
				
				success: updateListPage(params["tag"]),
			statusCode: {
				401: function() {
					show_login_dialog();
				}	    				    			
			},
			error: function(xhr, status, error) {
				var msg = $.parseJSON(xhr.responseText);
				$('.message').html(msg);
			}
		});
	} else {
		window.location = doc_url;
	}
}
   
        
		
function showMapView() {
	$('#observations_list_map').slideToggle(function() {

		if ($(this).is(':hidden')) {
			$('div.observations > div.observations_list').show();
			$('#map_view_bttn').css('background-color', 'transparent');
			$('#map_results_list > div.observations_list').remove();
			updateGallery(undefined, undefined, 0, undefined, window.params.isGalleryUpdate);
			// alert(" hiding map view");
			
		} else {
			$('div.observations > div.observations_list').hide();
			$('div.observations > div.observations_list').html('');
			// alert("showing map view");
		}

		google.maps.event.trigger(big_map, 'resize');
		big_map.setCenter(nagpur_latlng);
	});
}


