	//(function() {
	var $hilighted,	$hilightedMenuItem, optionDictionary ={}, names = [],
	toDot;

	toDot = function(id){
		return id.replace(/[-]+/g,'.');
	}; 

	function hilight(id) {
		var linkId, $el, $detailsWrap = $('#details-wrap');

		$el = $('div.member#' + id);
		// clear old
		if ($hilighted) {
			$hilighted.removeClass('hilighted');
		}
		if ($hilightedMenuItem) {
			$hilightedMenuItem.removeClass('hilighted');
		}

		if ($el.length > 0) {
			// hilight new
			$hilighted = $el;
			$hilighted.addClass('hilighted');
			$detailsWrap.scrollTop($hilighted.offset().top + $detailsWrap.scrollTop() - 160);
		}
		linkId = id.replace(/[^a-z0-9]+/gi,'.');
		$hilightedMenuItem = $('a.[href="#'+ linkId +'"]').not('.plus');
		$hilightedMenuItem.addClass('hilighted');

	}

	// expand and load children when necessary of current level
	var toggleExpand = function ($elem) {
		var $_menu = $elem.find('div[id$="-menu"]').first(), _id;

		_id = $_menu.attr('id').replace("-menu","");

        if ($elem.hasClass('collapsed')) {
            /* if not loaded, load children, standard we have three children */
            if ($_menu.children().size() == 1) {
                loadChildren(_id,false);
            }
            $elem.removeClass("collapsed");
            $elem.addClass("expanded");
            $_menu.slideDown();
            // show relevant section
            toggleSection(_id);
        } else {
            // hide children
            $_menu.slideUp('normal',function(){
                $elem.removeClass("expanded");
                $elem.addClass("collapsed");
            });
        }
    };

var toggleSection = function(sectionId){
	$section = $("#details > div.section:visible");
	// hide current section
	if($section){
		$section.hide();
	}
	$('#details > div.section#' + sectionId).show();
}


var addSectionOption = function(val){
	$section = $('<div class="section" id="' + val.name + '" style="display:none;"></div>').appendTo('#details');
    $('<h1>' + val.fullname + '</h1>'
    + (val.description ? '<div class="section-description">' + val.description + '</div>': '')
    + (val.demo ? '<div class="demo">Try it: ' + val.demo + '</div>': '' )).appendTo($section);

    /*
     * functionality not longer needed
     *
     *if(val.extending){
    	$('<h4 class="extends">Extends ' + toDot(val.extending) + '</h4>').appendTo($section);
    	// load inherited members
    	loadInheritedMembers($section, val.extending);
    }*/

    $(document).triggerHandler({ type:"xtra.btn.section.event",id: optionDictionary[val.fullname], table: 'option' });
}

var addSectionObject = function(val){
	$section = $('<div class="section" id="object-' + val.name + '" style="display:none;"></div>').appendTo('#details');
    $('<h2>' + val.title + ' ('+ val.name + ')</h2>').appendTo($section);
    $('<div class="section-description">' + val.description + '</div>').appendTo($section);
    $(document).triggerHandler({ type:"xtra.btn.section.event",id: 'object-'+ val.name, table: 'object'});
}

var loadOptionMemberInSection = function(obj, isParent){
	//add member to section in div#details
	var $_section = $('div#' + obj.parent + '.section'), $_inheritedLink;

    $('<div class="member" id="' + obj.name + '"><span class="title">' + obj.title + '</span>'
    		+ (obj.returnType ? '<span class="returnType">: ' 	+ obj.returnType.replace(/[<>]/g, function (a) {
    			return {
    				'<': '&lt;',
    				'>': '&gt;'
    			}[a];
    		}) + '</span>' : '')
    		+ (obj.deprecated ? '<div class="deprecated"><p>Deprecated</p></div>' : '' )
    		+ (obj.since ? '<div class="since">Since ' + obj.since + '</div>' : '' )
    		+ (obj.description ? '<div class="description">' + obj.description
    				+ (obj.defaults ? ' Defaults to <code>' + obj.defaults + '</code>.'  : '')
    				+ '</div>' : '')
    		+ (obj.demo ? '<div class="demo">Try it: ' + obj.demo + '</div>': '' )
    		+ (obj.seeAlso ? '<div class="seeAlso">See also: ' + obj.seeAlso + '</div>': '' )
    		+ '</div>').appendTo($_section);

    if (isParent){
    	$('div#' + obj.name + '.member span.title').html(function() {
    	    var title = $.trim($(this).text());
    	    return $('<a href="#' + obj.fullname + '">' + title + '</a>').click(function(){
            	gotoSection(obj.fullname, true);
            });
    	});
    }
    // remove  inherited members link when member is redefined
    /*
     * no longer inherited members, cleanup later...
     *  $_inheritedLink = $_section.find('dd a[href$="' + obj.title + '"]');
    if ($_inheritedLink.length > 0){
    	$_inheritedLink.remove();
    }
    */
}

var loadObjectMemberInSection = function(obj){
	$('<div class="member" id="' + obj.name + '">'
			+ '<span class="title">' + obj.title + '</span>'
			+ (obj.params ? '<span class="parameters">' + obj.params + '</span>' : '')
			+ (obj.since ? '<div class="since">Since ' + obj.since + '</div>' : '' )
			+ (obj.deprecated ? '<div class="deprecated"><p>Deprecated</p></div>' : '' )
			+ '<div class="description"><p>' + obj.description +  '</p>'
			+ (obj.paramsDescription ? '<h4>Parameters</h4><ul id="paramdesc"><li>' + obj.paramsDescription.replace(/\|\|/g,'</li><li>') + '</li></ul>' : '')
			+ (obj.returnType ? '<h4>Returns</h4><ul id="returns"><li>' + obj.returnType.replace(/\|\|/g,'</li><li>') + '</li></ul>' : '')
			+ '</div>'
			+ (obj.demo ? '<div class="demo">Try it: ' + obj.demo + '</div>': '' )
			+ '</div>').appendTo('div#object-' + obj.parent + '.section');
}

var loadChildren = function (name,silent) {
        $.ajax({
            type: "GET",
            url: RESOURCE + '/option/'+ PRODUCTNAME + '/child/' + name,
            async: false,
            dataType: "json",
            success: function (data) {
				var display = 'block',
				display, $menu, $menuItem;

				if(silent){
					display = 'none';
				}
				$menu = $('div#' + name + '-menu');

                $.each(data, function (key, val) {
                	var $div = $('<div></div>').appendTo($menu), $plus, $menuLink, parts,
                	tie, dottedName, internalName;

                	if (val.isParent) {
                		var preBracket = '{',
                			postBracket = '}';

                		if (val.returnType && val.returnType.indexOf('Array') === 0 ) {
                			preBracket = '[{';
                			postBracket = '}]';
                		}

                    	$menuItem = $('<div class="menuitem collapsed"></div>');
                        $menuLink = $('<a href="#' + val.fullname + '">' + val.title + '</a>').appendTo($menuItem);

                        $menuLink.click(function(){
                    		gotoSection(val.fullname, true);
                    	});
                        $plus = $('<a href="#' + val.fullname + '" class="plus"></a>').appendTo($menuItem);
                        $plus.click(function () {
                            toggleExpand($plus.parent());
                        });
                        $menuItem.append(':&nbsp;'+ preBracket +'<span class="dots">...</span>');
                        // add empty submenu
                        $subMenu = $('<div id="' + val.name + '-menu" style="display:none"><div>').appendTo($menuItem);
                        $menuItem.append(postBracket);
                        $menuItem.appendTo($menu);
                        addSectionOption(val);
                    }else {
	                    $menuLink = $('<a href="#' + val.fullname + '">' + val.title + '</a>').appendTo($div);
	                    $menuLink.click(function(){
	                    	gotoSection(val.fullname, true);
	                    });
	                    $('<span class="value">: ' + val.defaults + '</span>').appendTo($div);
                    }
                    loadOptionMemberInSection(val, val.isParent);
                });
                $(document).triggerHandler({ type:"xtra.btn.member.event", id: name, table: 'option'});
            }
        });
    };

    var loadObjectMembers = function(name){
    	//console.log(RESOURCE + '/object/'+ PRODUCTNAME + '-obj/child/' + name);
    	$.ajax({
            type: "GET",
            url: RESOURCE + '/object/'+ PRODUCTNAME + '-obj/child/' + name,
            async: false,
            dataType: "json",
            success: function (data) {
            	$.each(data, function (key, val) {
            		loadObjectMemberInSection(val);
            	});
    		}
    	});
    	$(document).triggerHandler({ type:"xtra.btn.member.event", id: 'object-' + name,table:'object'});
    };

    /* functionality not longer wanted
     * var loadInheritedMembers = function ($elem, idExtended) {
        $.ajax({
            type: "GET",
            url: '/my.url.com/option/' + PRODUCTNAME + '/child/' + idExtended,
            async: false,
            dataType: "json",
            success: function (data) {
            	var dd = [], i = 0, $deflist, dottedName, firstPart, indexDot;
            	// split the name
            	dottedName = toDot(idExtended)
            	indexDot = dottedName.lastIndexOf('.')
            	firstPart = dottedName.slice(0,indexDot+1);
            	lastPart = dottedName.slice(indexDot+1, dottedName.length);

            	$deflist = $('<dl id="inhMembers"><dt><bold>Members inherited from ' + firstPart + '<a href="#' + dottedName + '">' + lastPart  + '</a><bold></dt></dl>'),

                $.each(data, function (key, val) {
                	var name = val.title;
                	dd.push('<a href="#' + val.fullname + '">' + name);
                });
                $deflist.append('<dd>' + dd.join(",</a> ") + '</a></dd>');
                $elem.append($deflist);
                // enable click event for links
                $deflist.find('a').click(function(){
            		gotoSection($(this).attr('href').replace('#',''),true);
            	});
            }
        });
    };
    *
    */

	function gotoSection(anchor, hilighted) {
		var name, levels, member, isObjectArr, isObject, parts, $_parent, $_menu, sectionId, parent;
		// is it an option-section or an object-section?
		parts = anchor.split("-");
		name = optionDictionary[anchor];

		isObject = (parts.length > 1 && parts[0] == 'object' || (name && name.indexOf('::') > 0));


		if (!isObject && name){
			member = name.split("--");
	        levels = member[0].split("-");

	        // expand parent elements of selected item
	        for (i = 0; i < levels.length; i++) {
	            if (levels[i]) {
	                level = levels.slice(0, i + 1).join('-');
	                $_menu = $('#' + level + '-menu');
	                $_parent = $_menu.parent();
	                if ($_menu != 'undefined' && $_menu !== null) {



	                	if ($_parent.hasClass('collapsed')) {
	                		toggleExpand($_parent);
	                	}
	                	if (hilighted) {
	                		setTimeout(function () { // jQuery.show() runs in a timeout, have to wait for collapse
	                			hilight(name);
	                		}, 0);
	                	}

	                    // empty search
	                    $("#search").val("");
	                    window.location.hash = anchor;
	                }
	            }

	            if(1 + i == levels.length){
	            	toggleSection(level);
	            }
	        }
       } else if (isObject) {
    	   if (name && name.indexOf('::') > 0){
    		   sectionId = 'object-' + name.split("::")[0];

    		   parent = anchor.split(".")[0];
    		}else{
    			sectionId = anchor;
    			parent = anchor.split("-")[1];
    		}

    	   $section = $('#' + sectionId);
    	   // has section member loaded?
    	   if ($section.children('div.member').length == 0){
    		   loadObjectMembers(parent);
    	   }

    	   toggleSection(sectionId);
    	   window.location.hash = anchor;
       }
	}

    // Startup
    $(document).ready( function () {

    	// convert hash from redirected dash syntax to new dot syntax
    	if (/-/.test(location.hash)) {
    		location.hash = location.hash.replace(/(--|-)/g, '.');
    	}

    	// autocomplete
    	$.ajax({
            type: "GET",
            /*url: RESOURCE + '/option/' + PRODUCTNAME + '/names',*/
            url: RESOURCE + '/' + PRODUCTNAME + '/names',
            async: false,
            dataType: "json",
            success: function (data) {
            	$.each(data, function (key, val) {
    	        	names.push(key);
    	        	optionDictionary[key] = val;
            	});

    	        $("#search" ).autocomplete({
    	        	source: names,
    	        	minLength: 2,
    				select: function( event, ui ) {
    						gotoSection(ui.item.value, true);
    				}
    	        });
            }
        });

        // load main options and build folded menu tree
    	$.ajax({
            type: "GET",
            url: RESOURCE + '/option/' + PRODUCTNAME + '/main',
            async: false,
            dataType: "json",
            success: function (data) {
            	$.each(
            			data, function (key, val) {

            				//var $opt = $('<div class="menuitem collapsed" id="' + val.name + '"></div>').appendTo('#options'),
            				var $menuItem = $('<div class="menuitem collapsed"></div>').appendTo('#options'),
            					$plus, anchor, $menu, levels, level, member, $menuLink,
            					prefix = '{',
            					suffix = '}';

            				if (val.returnType && val.returnType.indexOf('Array') == 0) {
            					prefix = '[{';
            					suffix = '}]';
            				}


            				// store names in array for translation
            				// optionDictionary[val.fullname] = val.name;

            				$menuLink = $('<a href="#' + val.fullname + '">' + val.title + '</a>').appendTo($menuItem);
            				$menuLink.click(function(){
            					gotoSection(val.fullname, true);
            				});
            				$plus = $('<a href="#' + val.fullname + '" class="plus"></a>').appendTo($menuItem);
            				$plus.click(function () {
            					toggleExpand($plus.parent());
            				});
            				$menuItem.append(':&nbsp;' + prefix);
            				$('<span class="dots">...</span>').appendTo($menuItem);
            				$subMenu = $('<div id="' + val.name + '-menu" style="display:none"><div>').appendTo($menuItem);
            				$menuItem.append(suffix);
            				// create sections in div#details
            				addSectionOption(val);
            			});
            }
        });

    	// load objects of product
    	$.ajax({
            type: "GET",
            url: RESOURCE + '/object/' + PRODUCTNAME + '-obj/main',
            async: false,
            dataType: "json",
            success: function (data) {
	        	var $objectTOC = $('#methods-and-properties-toc'), $ul;
	    		$ul = $('<ul></ul>').appendTo($objectTOC);
	        	$.each(data, function(key, obj){
	            	// store names in array for translation
	            	//optionDictionary['object.' + obj.name] = obj.name;
	        		var $li = $('<li></li>').appendTo($ul), $menuLink;
	        		$menuLink = $('<a href="#object-' + obj.name +  '">' + obj.title + '</a>').appendTo($li);
	                $menuLink.click(function(){
	            		gotoSection('object-' + obj.name, false);
	            	});
	                addSectionObject(obj);
	        	});
            }
        });

         // check url for anchor
         anchor = window.location.hash.replace('#', '');
         if (anchor) {
            gotoSection(anchor, true);
         }

         // focus search
         $("#search")[0].focus()
    });

//}());


