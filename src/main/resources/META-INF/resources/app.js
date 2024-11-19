var autoRefreshIntervalId = null;
const dateTimeFormatter = JSJoda.DateTimeFormatter.ofPattern('HH:mm')

let demoDataId = null;
let scheduleId = null;
let loadedSchedule = null;

var calendarMap = new Map();

$(document).ready(function () {
  var calendarEl = document.getElementById('calendar');
  var calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: 'timeGrid',
  });
  calendar.render();
  calendar.gotoDate('2024-01-01');

  $("#solveButton").click(function () {
    solve();
  });
  $("#stopSolvingButton").click(function () {
    stopSolving();
  });
  $("#analyzeButton").click(function () {
    analyze();
  });

  setupAjax();
  fetchDemoData();

  function setupAjax() {
    $.ajaxSetup({
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json,text/plain', // plain text is required by solve() returning UUID of the solver job
      }
    });

    // Extend jQuery to support $.put() and $.delete()
    jQuery.each(["put", "delete"], function (i, method) {
      jQuery[method] = function (url, data, callback, type) {
        if (jQuery.isFunction(data)) {
          type = type || callback;
          callback = data;
          data = undefined;
        }
        return jQuery.ajax({
          url: url,
          type: method,
          dataType: type,
          data: data,
          success: callback
        });
      };
    });
  }

  function fetchDemoData() {
    $.get("/demo-data", function (data) {
      // load first data set
      demoDataId = data[0];

      refreshSchedule();
    }).fail(function (xhr, ajaxOptions, thrownError) {
      // disable this page as there is no data
      let $demo = $("#demo");
      $demo.empty();
      $demo.html("<h1><p align=\"center\">No test data available</p></h1>")
    });
  }

  var firstLoad = true;

  function refreshSchedule() {
    let path = "/timetables/" + scheduleId;
    if (scheduleId === null) {
      if (demoDataId === null) {
        alert("Please select a test data set.");
        return;
      }

      path = "/demo-data/" + demoDataId;
    }

    $.getJSON(path, function (schedule) {
      loadedSchedule = schedule;

      if (firstLoad) {
        $.each(schedule.beamlines, (_, beamline) => {
          var calendarDomId = `calendar-${beamline.id}`;
          $('#calendars').append(`<h2>${beamline.name}</h2><div id="${calendarDomId}"></div>`);

          var calendarEl = document.getElementById(calendarDomId);
          var calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'timeGridWeek',
          });
          calendar.render();
          calendar.gotoDate('2024-01-01');
          calendarMap.set(beamline.id, calendar);
        });
        firstLoad = false;
      }

      renderSchedule(schedule);
    })
      .fail(function (xhr, ajaxOptions, thrownError) {
        showError("Getting the timetable has failed.", xhr);
        refreshSolvingButtons(false);
      });
  }

  function renderSchedule(timetable) {
    refreshSolvingButtons(timetable.solverStatus != null && timetable.solverStatus !== "NOT_SOLVING");
    $("#score").text("Score: " + (timetable.score == null ? "?" : timetable.score));

    calendarMap.forEach((cal, k, map) => {
      var sessions = timetable.sessions.filter(session => session.beamline === k);

      refreshSessions(cal, sessions)
    });
    refreshSessions(calendar, timetable.sessions, false)
  }

  function refreshSessions(cal, sessions, colorPerProposal = true) {
    cal.removeAllEvents();
    cal.batchRendering(function () {
      $.each(sessions, (_, session) => {
        if (!!session.beamtimeSlot?.startsAt) {
          cal.addEvent({
            id: session.id,
            title: `${session.beamline} - ${session.proposal.finalNumber} - ${session.localContact?.staffMember?.name}`,
            start: session.beamtimeSlot.startsAt,
            end: session.beamtimeSlot.endsAt,
            backgroundColor: colorPerProposal ? pickColor(session.proposal.finalNumber) : pickColor(session.beamline)
          });
        }
      });
    });
  }

  function solve() {
    $.post("/timetables", JSON.stringify(loadedSchedule), function (data) {
      scheduleId = data;
      refreshSolvingButtons(true);
    }).fail(function (xhr, ajaxOptions, thrownError) {
      showError("Start solving failed.", xhr);
      refreshSolvingButtons(false);
    },
      "text");
  }

  function analyze() {
    new bootstrap.Modal("#scoreAnalysisModal").show()
    const scoreAnalysisModalContent = $("#scoreAnalysisModalContent");
    scoreAnalysisModalContent.children().remove();
    if (loadedSchedule.score == null || loadedSchedule.score.indexOf('init') != -1) {
      scoreAnalysisModalContent.text("No score to analyze yet, please first press the 'solve' button.");
    } else {
      $('#scoreAnalysisScoreLabel').text(`(${loadedSchedule.score})`);
      $.put("/timetables/analyze", JSON.stringify(loadedSchedule), function (scoreAnalysis) {
        let constraints = scoreAnalysis.constraints;
        constraints.sort((a, b) => {
          let aComponents = getScoreComponents(a.score), bComponents = getScoreComponents(b.score);
          if (aComponents.hard < 0 && bComponents.hard > 0) return -1;
          if (aComponents.hard > 0 && bComponents.soft < 0) return 1;
          if (Math.abs(aComponents.hard) > Math.abs(bComponents.hard)) {
            return -1;
          } else {
            if (aComponents.medium < 0 && bComponents.medium > 0) return -1;
            if (aComponents.medium > 0 && bComponents.medium < 0) return 1;
            if (Math.abs(aComponents.medium) > Math.abs(bComponents.medium)) {
              return -1;
            } else {
              if (aComponents.soft < 0 && bComponents.soft > 0) return -1;
              if (aComponents.soft > 0 && bComponents.soft < 0) return 1;

              return Math.abs(bComponents.soft) - Math.abs(aComponents.soft);
            }
          }
        });
        constraints.map((e) => {
          let components = getScoreComponents(e.weight);
          e.type = components.hard != 0 ? 'hard' : (components.medium != 0 ? 'medium' : 'soft');
          e.weight = components[e.type];
          let scores = getScoreComponents(e.score);
          e.implicitScore = scores.hard != 0 ? scores.hard : (scores.medium != 0 ? scores.medium : scores.soft);
        });
        scoreAnalysis.constraints = constraints;

        scoreAnalysisModalContent.children().remove();
        scoreAnalysisModalContent.text("");

        const analysisTable = $(`< table class= "table" /> `).css({ textAlign: 'center' });
        const analysisTHead = $(`< thead /> `).append($(` < tr /> `)
          .append($(`< th ></th > `))
          .append($(`< th > Constraint</th > `).css({ textAlign: 'left' }))
          .append($(`< th > Type</th > `))
          .append($(`< th ># Matches</th > `))
          .append($(`< th > Weight</th > `))
          .append($(`< th > Score</th > `))
          .append($(`< th ></th > `)));
        analysisTable.append(analysisTHead);
        const analysisTBody = $(`< tbody /> `)
        $.each(scoreAnalysis.constraints, (index, constraintAnalysis) => {
          let icon = constraintAnalysis.type == "hard" && constraintAnalysis.implicitScore < 0 ? '<span class="fas fa-exclamation-triangle" style="color: red"></span>' : '';
          if (!icon) icon = constraintAnalysis.weight < 0 && constraintAnalysis.matches.length == 0 ? '<span class="fas fa-check-circle" style="color: green"></span>' : '';

          let row = $(`< tr /> `);
          row.append($(`< td /> `).html(icon))
            .append($(`< td /> `).text(constraintAnalysis.name).css({ textAlign: 'left' }))
            .append($(`< td /> `).text(constraintAnalysis.type))
            .append($(`< td /> `).html(` < b > ${constraintAnalysis.matches.length}</b > `))
            .append($(`< td /> `).text(constraintAnalysis.weight))
            .append($(`< td /> `).text(constraintAnalysis.implicitScore));

          analysisTBody.append(row);
          row.append($(`< td /> `));
        });
        analysisTable.append(analysisTBody);
        scoreAnalysisModalContent.append(analysisTable);
      }).fail(function (xhr, ajaxOptions, thrownError) {
        showError("Analyze failed.", xhr);
      },
        "text");
    }
  }

  function getScoreComponents(score) {
    let components = { hard: 0, medium: 0, soft: 0 };

    $.each([...score.matchAll(/(-?[0-9]+)(hard|medium|soft)/g)], (i, parts) => {
      components[parts[2]] = parseInt(parts[1], 10);
    });

    return components;
  }


  function refreshSolvingButtons(solving) {
    if (solving) {
      $("#solveButton").hide();
      $("#stopSolvingButton").show();
      if (autoRefreshIntervalId == null) {
        autoRefreshIntervalId = setInterval(refreshSchedule, 2000);
      }
    } else {
      $("#solveButton").show();
      $("#stopSolvingButton").hide();
      if (autoRefreshIntervalId != null) {
        clearInterval(autoRefreshIntervalId);
        autoRefreshIntervalId = null;
      }
    }
  }

  function stopSolving() {
    $.delete("/timetables/" + scheduleId, function () {
      refreshSolvingButtons(false);
      refreshSchedule();
    }).fail(function (xhr, ajaxOptions, thrownError) {
      showError("Stop solving failed.", xhr);
    });
  }
});
