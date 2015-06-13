/*Grabs up to request.params.numberOfDrawings drawings from the
Parse database within the provided radius (request.params.radius).*/
Parse.Cloud.define("nearbyDrawings", function(request, response) {	
	var queryDistance = request.params.radius;

	var query = new Parse.Query("DrawingItem");
	query.withinKilometers("location", request.params.myLocation, queryDistance);
	query.notContainedIn("objectId", request.params.currentDrawings);
	query.limit(request.params.numberOfDrawings);
	query.include("creator");
	query.find({
		success: function(results) {
			response.success(results);
		},
		error: function() {
			response.error("Drawings Query Failed");
		}
	});
});

/*Returns all of the drawings that are in request.params.currentDrawings
but are no longer within the provided radius (request.params.radius).*/
Parse.Cloud.define("getDrawingsNoLongerNearby", function(request, response) {  
	var queryDistance = request.params.radius;
	
	var query = new Parse.Query("DrawingItem");
	query.withinKilometers("location", request.params.myLocation, queryDistance);
	query.containedIn("objectId", request.params.currentDrawings);
	query.find({
		success: function(results) {
			var resultsToReturnAndDelete = [];
			var resultNumber = 0;
			var currDrawings = request.params.currentDrawings;
			var hasDrawing = 0;

			for (var i = 0; i < currDrawings.length; i++)
			{
				for (var j = 0; j < results.length; j++)
				{
					if (currDrawings[i] === results[j].id)
					{
						hasDrawing = 1;
						break;
					}
				}
					
					//drawing is in current drawings but not query results
				if (hasDrawing == 0){
					resultsToReturnAndDelete[resultNumber] = currDrawings[i];
					resultNumber++;
				}
				hasDrawing = 0;
			}

			response.success(resultsToReturnAndDelete);
		},
		error: function() {
			response.error("Drawings Query Failed");
		}
	});
});
