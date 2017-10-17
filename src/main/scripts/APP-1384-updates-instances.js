function getActiveInstances() {
    var instances = [];

    db.integrationconfiginstance.find({"active": true}).forEach(function(instance) {
        instances.push(instance);
    });

    return instances;
}

function normalizeStreams(streams) {
    return streams.map(function(stream) {
        var result = '';

        for (var i = 0; i < stream.length; i++) {
            var char = stream.charAt(i);

            if (char === '-') {
                result += '+';
            } else if (char === '_') {
                result += '/';
            } else {
                result += char;
            }
        }

        return result.concat('==');
    });
}

function updateInstance(instance) {
    var streams = instance.optionalProperties.streams;
    var normalizedStreams = normalizeStreams(streams);
    var rooms = instance.optionalProperties.rooms;
    var shouldUpdate = false;

    for (var i = 0; i < rooms.length; i++) {
        var streamId = rooms[i].streamId;

        if (streamId !== undefined && normalizedStreams.indexOf(streamId) === -1) {
            var normalized = streamId.replace(new RegExp('/', 'g'), '_').substring(0, streamId.length - 2);
            streams.push(normalized);

            shouldUpdate = true;
        }
    }

    if (shouldUpdate === true) {
        var result = db.integrationconfiginstance.update({ "_id" : instance._id}, { $set: { "optionalProperties.streams" : streams } });
        return result.nModified;
    }

    return 0;
}

function updateInstances() {
    var instances = getActiveInstances();
    var updated = 0;

    for (var i = 0; i < instances.length; i++) {
        var instance = instances[i];

        var optionalProperties = instance.optionalProperties;

        if (optionalProperties !== undefined && optionalProperties.rooms !== undefined) {
            updated += updateInstance(instance);
        }
    }

    return updated;
}

function validateInstance(instance) {
    var optionalProperties = instance.optionalProperties || {};

    if (optionalProperties.rooms === undefined) {
        return true;
    }

    var rooms = optionalProperties.rooms || [];
    var streams = optionalProperties.streams || [];

    var normalizedStreams = normalizeStreams(instance.optionalProperties.streams);

    for (var i = 0; i < rooms.length; i++) {
        var streamId = rooms[i].streamId;

        if (streamId !== undefined && normalizedStreams.indexOf(streamId) === -1) {
            return false;
        }
    }

    return true;
}

function validate(instacesUpdated) {
    var message = "Successfully updated " + instacesUpdated + " webhooks";
    var response = true;
    var instances = getActiveInstances();

    for (var i = 0; i < instances.length; i++) {
        var instance = instances[i];

        response = validateInstance(instance);
        if (response == false) {
            message = "Failed to update instance: " + instance._id;
            break;
        }
    }

    printjson({
        response: response,
        message: message
    });
}

var result = updateInstances();
validate(result);