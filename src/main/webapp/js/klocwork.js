var DefaultOptions = {
    duration: 0.3   // seconds
};

const issueLinkElementId = "issueLinkElement";
const issueLinkId = "issueLink";
const issueTraceId = "issueTrace";
const issueCitationDivId = "issueCitationModal";
const issueDetailsId = "fullIssueDetails";

var Klocwork = {
    /**
     * Toggles the visibility of an element
     * @param control - the widget that toggles visibility
     * @param toToggle - the element to show/hide
     */

    toggle: function (control, toToggle) {
        var elementToToggle = $(toToggle);
        if (!control.hasClassName('collapsible'))
            return;
        if (elementToToggle.hasClassName('collapsed')) {
            Effect.SlideDown(toToggle, {duration: DefaultOptions.duration, queue: 'end', limit: 1 });
            elementToToggle.removeClassName('collapsed');
            elementToToggle.addClassName('expanded');
            control.addClassName('expanded');
            control.removeClassName('collapsed');
        } else {
            Effect.SlideUp(toToggle, {duration: DefaultOptions.duration, queue: 'end', limit: 1 });
            elementToToggle.removeClassName('expanded');
            elementToToggle.addClassName('collapsed');
            control.addClassName('collapsed');
            control.removeClassName('expanded');
        }
    },

    showFullIssue: function (issueIds, user, ltoken, issueLinkId, issueTraceId, issueLinkElementId) {
        function createTraceLines(lines, file, ul) {
            for (var lineIndex = 0; lineIndex < lines.length; lineIndex++) {
                var traceLine = document.createElement("samp");
                var line = lines[lineIndex].line;
                var text = lines[lineIndex].text;
                var li = document.createElement("li");
                li.addClassName("issue-trace-line");
                ul.appendChild(li);

                //create line as:
                //<file>:<line#> - <text>
                var lineText = line + " - " + text;
                if (file) {
                    lineText = file + ":" + lineText;
                }
                traceLine.appendChild(document.createTextNode(lineText));
                li.appendChild(traceLine);

                if (lines[lineIndex].trace && lines[lineIndex].trace.length > 0) {
                    var subTrace = document.createElement("ul");
                    subTrace.addClassName("list-style-none");
                    createTrace(lines[lineIndex].trace, subTrace);
                    ul.appendChild(subTrace);
                }
            }
        }

        function createTrace(trace, ul) {
            for (var traceIndex = 0; traceIndex < trace.length; traceIndex++) {
                var lines = trace[traceIndex].lines;
                var file = trace[traceIndex].file;
                if (file) {
                    //get the file name, remove parent path
                    var start = file.lastIndexOf("/");
                    if (start > 0 && start != file.length) {
                        //normalize to slash path separators
                        file = file.split("\\").join("/");
                        file = file.substr(start + 1);
                    }
                }
                createTraceLines(lines, file, ul);
            }
            return {traceIndex: traceIndex, lines: lines};
        }

        //noinspection JSUnresolvedFunction
        klocworkResultsAction.getIssueDetails(issueIds, "", user, ltoken, function (t) {
            var detailedIssues = t.responseJSON;
            if (detailedIssues == null) return;
            for (var i = 0; i < detailedIssues.length; i++) {
                var issue = detailedIssues[i];
                var traceSection = document.createElement("div");
                var issueDetails = document.getElementById(issueDetailsId + issue.id);

                var ul = document.createElement("ul");
                ul.addClassName("list-style-none");

                //create trace section
                traceSection.id = issueTraceId + issue.id;
                traceSection.addClassName("issue-trace-block collapsed");

                issueDetails.appendChild(traceSection);

                var trace = issue.trace;

                if (trace && trace.length > 0) {
                    createTrace(trace, ul);
                    traceSection.appendChild(ul);

                    //update issue link to be collapsible
                    var issueLinkElement = document.getElementById(issueLinkElementId + issue.id);
                    issueLinkElement.removeClassName("no-trace");
                    issueLinkElement.addClassName("collapsible");
                }

                var lineElement = document.getElementById("issueLine" + issue.id);
                if (issue.line != 0) {
                    lineElement.innerHTML = " Line " + issue.line + ": ";
                }

                Klocwork.showCitationButton(issue);
            }
        });
    },

    getIssueIds: function (issueLinks) {
        var issueIds = [];

        for (var i = 0; i < issueLinks.length; i++) {
            var traceId = issueLinks[i].id;
            var issueId = traceId.substring(traceId.indexOf(issueLinkElementId) + issueLinkElementId.length);

            var traceSection = document.getElementById(issueTraceId + issueId);
            if (traceSection) continue;

            issueIds.push(parseInt(issueId));
        }
        return issueIds;
    },

    /**
     * Updates the issue results with detailed results (ie. show trace).
     * Shows the citing UI for each issue.
     * @param user - the user that is authenticated with Klocwork Server
     * @param ltoken - the ltoken for communicating with Klocwork Server
     * */
    updateIssues: function updateIssues(user, ltoken) {
        var issueLinks = document.querySelectorAll("[id^=" + issueLinkElementId + "]");
        var issueIds = this.getIssueIds(issueLinks);

        if (issueIds.length == 0) return;

        this.showFullIssue(issueIds, user, ltoken, issueLinkId, issueTraceId, issueLinkElementId);
    },

    /**
     * Shows the 'Cite' button next to the issue link
     * @param issue - the issue id
     */
    showCitationButton: function (issue) {
        var issueStatus = document.getElementById("issueStatus" + issue.id);
        issueStatus.value = issue.status;
        Effect.Appear("showIssueCitation" + issue.id, {duration: 0.1});
    },

    /**
     * Show the citation controls for the given issue.
     * @param issueId - the issue to show citation controls
     */
    showCitation: function (issueId) {
        var citationModal = document.getElementById(issueCitationDivId + issueId);
        citationModal.style.display = "block";
        $(citationModal).setOpacity(0);
        new Effect.Opacity(citationModal, {from: 0.0, to: 1.0});
    },

    hideCitationModal: function (issueId) {
        Effect.Fade(issueCitationDivId + issueId);
        var citeError = document.getElementById("citeError" + issueId);
        citeError.innerHTML = "";
        document.getElementById("load-indicator").className="loading-overlay-off";
    },

    /**
     * Save the citation for the given issue.
     * @param issueId - the issue to save
     */
    saveCitation: function (issueId) {

        document.getElementById("load-indicator").className = "loading-overlay-on";

        var comment = document.getElementById("issueComment" + issueId).value;
        var newStatus = document.getElementById("issueStatus" + issueId).value;

        klocworkResultsAction.citeIssue(issueId, newStatus, comment, function(t) {

            var responseJSON = t.responseJSON;
            if (!responseJSON.result) {
                var citeError = document.getElementById("citeError" + issueId);
                citeError.innerHTML = responseJSON.error;
                return;
            }

            var commentText = document.getElementById("issueComment" + issueId);
            commentText.value = "";

            Klocwork.hideCitationModal(issueId);

        });


    },

    cancelCitation: function (issueId) {
        this.hideCitationModal(issueId);
    }

};
