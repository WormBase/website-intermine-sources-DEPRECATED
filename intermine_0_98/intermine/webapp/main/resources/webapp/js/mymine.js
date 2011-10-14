
   function selectColumnCheckbox(form, type) {
       	var deleteButton = document.getElementById('delete_button');
   		var removeButton = document.getElementById('remove_button');
  	 	var exportButton = document.getElementById('export_button');

       var columnCheckBox = 'selected_' + type;
       var checked = document.getElementById(columnCheckBox).checked;

       if (deleteButton != null) {
           deleteButton.disabled = !checked;
       }
       if (removeButton != null) {
           removeButton.disabled = !checked;
       }
       if (exportButton != null) {
           exportButton.disabled = !checked;
       }
       with(form) {
           for(var i=0;i < elements.length;i++) {
               var thiselm = elements[i];
               var testString = columnCheckBox + '_';
               if(thiselm.id.indexOf(testString) != -1)
                   thiselm.checked = checked;
           }
       }
   }


   function setDeleteDisabledness(form, type) {

       	var deleteButton = document.getElementById('delete_button');
   		var removeButton = document.getElementById('remove_button');
  	 	var exportButton = document.getElementById('export_button');

       var checkBoxPrefix = 'selected_' + type + '_';
       var deleteDisable = true;
       var columnCheckBoxChecked = true;
       with(form) {

           for(var i=0;i < elements.length;i++) {
               var thiselm = elements[i];

               if (thiselm.id.indexOf(checkBoxPrefix) != -1) {
                   if (thiselm.checked) {
                       deleteDisable = false;
                   } else {
                       columnCheckBoxChecked = false;
                   }
               }
           }
       }
       if (deleteButton != null) {
           deleteButton.disabled = deleteDisable;
       }
       if (removeButton != null) {
           removeButton.disabled = deleteDisable;
       }
       if (exportButton != null) {
           exportButton.disabled = deleteDisable;
       }
       document.getElementById('selected_' + type).checked = columnCheckBoxChecked;
       return true;
   }


  function noenter() {
    return !(window.event && window.event.keyCode == 13);
  }

  jQuery(function() {
  	jQuery('#newApiKeyButton').unbind("click").click(function() {
		AjaxServices.generateApiKey($CURRENT_USER, function(response) {
			jQuery('span.apikey').text(response).removeClass("nokey");
			jQuery('#deleteApiKeyButton').show();
		})
	})
	jQuery('#deleteApiKeyButton').unbind("click").click(function() {
		AjaxServices.deleteApiKey($CURRENT_USER, function(response) {
			if (response === "deleted") {
				jQuery('span.apikey').text("Key Deleted").addClass("nokey");
				jQuery('#deleteApiKeyButton').hide();
			}
		})
	})
  });

  jQuery(function() {
      var selector = jQuery('#classNameSelector');
      selector.unbind("change").change(function() {
          var classname = selector.val();
          jQuery('div.classLabels').each(function() {
              if (this.id === 'labelsFor' + classname) {
                  jQuery(this).show();
              } else {
                  jQuery(this).hide();
              }
          });
      });
  });

