var la_db = null;
var la_timeout = null;
var la_prop = null;

function la_init() {
	la_init_clock();
	la_width();
    la_load();
    la_get_data();
    la_online_users(true);
}

function la_online_users(only_user_count, page) {
	
	if ($("#lta_online_users table").length >0 && !page) {
		$("#lta_online_users").html("");
		return;
	}
	
	var settings = la_ajax_setting();
	settings["url"] = $("#url").val()+"/agent/onlineusers";
	settings["success"] = function(d) {
		if (d.status == "OK") {
			
			var table = 

				"<div style=\"width: 100%; margin-top: 5px; margin-bottom: 5px; text-align: right\">\n" +
				"  <button type=\"button\" class=\"btn btn-danger btn-sm\" onclick=\"la_online_users()\">&#x274C;</button>\n" + 
				"</div>" +

				"<table class=\"table table-bordered\">\n" +
				"  <thead>\n" + 
				"    <tr>\n" + 
				"      <th>#</th>\n" + 
				"      <th>Kullanıcı</th>\n" + 
				"      <th>Oturum</th>\n" + 
				"      <th>Hostname</th>\n" + 
				"      <th>IP Adres</th>\n" + 
				"      <th>Ahenk DN</th>\n" + 
				"    </tr>\n" + 
				"   </thead>\n" + 
				"  <tbody>\n";
			
			$("#online_users").html(d.resultMap.onlineUsers.length);
			if (!only_user_count) {
				la_message("Kullanıcı listesi alındı.","success");

				var n = d.resultMap.onlineUsers.length;
				for (var i=0; i<n; i++) {
					var x = d.resultMap.onlineUsers[n-i-1];
					var date = new Date(x.createDate);
					// Hours part from the timestamp
					var hours = date.getHours();
					// Minutes part from the timestamp
					var minutes = "0" + date.getMinutes();
					// Seconds part from the timestamp
					var seconds = "0" + date.getSeconds();

					// Will display time in 10:30:23 format
					var formattedTime = lpad(date.getDate(), 2)+"/"+lpad(date.getMonth()+1,2)+"/"+ date.getFullYear() +"&nbsp;" +hours + ':' + minutes.substr(-2) + ':' + seconds.substr(-2);
					table += "<tr onclick=\"la_ahenk_info("+x.agentId+")\">"+
							 "<td>"+(i+1)+"</td>"+
							 "<td>"+x.username+"</td>"+
							 "<td>"+formattedTime+"</td>"+
							 "<td>"+x.hostname+"</td>"+
							 "<td>"+x.ipAddresses+"</td>"+
							 "<td style='white-space: nowrap; font-size: 90%'>"+x.dn+"</td>"+
							 "</tr>";
				}
			}
			else {
				return;
			}
			
			table += "  </tbody>\n" + 
					 "</table>" + 
						"<div style=\"width: 100%; margin-top: 5px; margin-bottom: 5px; text-align: right\">\n" +
						"  <button type=\"button\" class=\"btn btn-danger btn-sm\" onclick=\"la_online_users()\">&#x274C;</button>\n" + 
						"</div>";

			$("#lta_online_users").html(table);
		}
		else {
			la_message("Kullanıcı listesi hatalı.","danger");
			$("#lta_online_users").html("");
		}
		
	},
	settings["error"] = function() {
		la_message("Kullanıcı listesi hatalı.","danger");
		$("#lta_online_users").html("");
	}
	$.ajax(settings).done(function(response) {
		console.log(response);
	});

	
}

function la_ahenk_info(agent_id) {
	
}

function la_ahenk_prop() {
	var p = $("#lta_ahenk_prop").val();
	
	if (p) {
		var map = {};
		$.each(la_db.agents, function (x, agent) {
			var t = false;
			$.each(agent.properties, function(x, prop) {
				if (prop.propertyName == p) {
					if (map[prop.propertyValue]) {
						map[prop.propertyValue] ++;
					}
					else {
						map[prop.propertyValue] = 1;
					}
					t = true;
				}
			});
			if (!t) {
				if (map["N/A"]) {
					map["N/A"] ++;
				}
				else {
					map["N/A"] = 1;
				}
			}
		});
		$("#lta_ahenk_prop_tbody").attr("property", p);
		$("#lta_ahenk_prop_tbody").html("");
		for (x in map) {
			var tr = $("<tr style='cursor: pointer' class='lta_tr' p='"+p+"' x='"+(x?x:"N/A")+"'><td>"+x+"</td><td>"+map[x]+"</td></tr>");
			tr.click( function () {
				la_list_prop($(this).attr("p"), $(this).attr("x"));
			});
			$("#lta_ahenk_prop_tbody").append(tr);
		}
		$("#lta_ahenk_prop_div").show();
		$("#lta_ahenk_prop_div").height($("#lta_ahenk_prop_table").height()+50);
		$('table.highchart').highchartTable();
	}
	else {
		$("#lta_ahenk_prop_div").hide();
	}
}

function la_set_column(obj) {
	
	localStorage.setItem("lc_"+$(obj).val().replace(/\./g,"_"), $(obj).is(":checked") ? "1" : "0");
	if ($(obj).is(":checked")) {
		$(".lc_"+$(obj).val().replace(/\./g,"_")).show();
	}
	else {
		$(".lc_"+$(obj).val().replace(/\./g,"_")).hide();
	}
}

function la_list_prop(p, l) {
	var prop = la_prop;
	var tr = $("<tr></tr>");
	tr.append("<th>#</th>");
	var list = [];
	$.each(prop.properties, function (i, x) {
		list.push(i);
	});
	list.sort();
	$.each(list, function (i, x) {
		var value = list[i];
		var label = value;
		if (label == 'hardware.disk.partitions') {
			return;
		}
		tr.append("<th class='lc_"+label.replace(/\./g,"_")+"'>"+label+"</th>");
	}) ;
	$("#lta_ahenk_prop_table2").html("<thead></thead><tbody></tbody>");
	$("#lta_ahenk_prop_table2 thead").append(tr);
	var i = 0;
	

	$.each(la_db.agents, function (x, agent) {
		var v = agent.pmap[p];
		if (!v)
			v = "N/A";
		if (v == l) {
			tr = $("<tr></tr>");
			i++;
			tr.append("<td>"+i+"</td>");
			$.each(list, function (i, p) {
				var value = list[i];
				var label = value;
				if (label == 'hardware.disk.partitions') {
					return;
				}
				tr.append("<td class='lc_"+label.replace(/\./g,"_")+"'>"+(agent.pmap[label] ? agent.pmap[label] : "N/A")+"</td>");
			}) ;
			$("#lta_ahenk_prop_table2 tbody").append(tr);
		}
		
	});
	
	$("#lta_display_columns").show();
	
	
	$("#lta_columns").html("");
	$.each(list, function (i, p) {
		var value = list[i];
		var label = value;
		if (label == 'hardware.disk.partitions' ||
			label == 'os.name' ||
			label == 'hostname' ||
			label == 'macAddresses' ||
			label == 'os.kernel' ||
			label == 'os.version'
			)  {
			return;
		}
		var checked = localStorage.getItem("lc_"+label.replace(/\./g,"_"));
		console.log(checked);
		if (checked == "1") {
			checked = " checked='checked' ";
			$(".lc_"+label.replace(/\./g, "_")).show();
		}
		else {
			$(".lc_"+label.replace(/\./g, "_")).hide();
		}
		$("#lta_columns").append(
				'<div style="width: 250px; float: left; margin-right: 15px">'+
					'<label><input type="checkbox" value="'+label+'" style="float:left" onclick="la_set_column(this)" '+checked+'><span style="position: relative; top: 2px">'+label+'</span></label>'+
				'</div>');
	});
	
}

function la_refresh_data() {
	var prop = {
		total_agent: 0,
		online_agent: 0,
		online_agents_by_day: [0,0,0,0,0,0,0,0,0,0],
		total_agents_by_day: [0,0,0,0,0,0,0,0,0,0],
		properties: {}
	};
	var now = new Date();
	var timestamp = now.getTime();
	var gun = Math.floor(timestamp/1000/60/60/24);
	var last_agent = null;
	console.log(la_db);
	$.each(la_db.agents, function (x, agent) {
		var g = Math.floor(agent.createDate/1000/60/60/24);
		var n = gun-g;
		if (n > prop.total_agents_by_day.length) {
			n = prop.total_agents_by_day.length-1;
		}
		if (!agent.deleted) {
			for (var i=n; i>=0; i--) {
				prop.total_agents_by_day[i] ++;
			}
		}
		var online = [0,0,0,0,0,0,0,0,0,0];
		$.each(agent.sessions, function(x, session) {
			if (session.sessionEvent = "LOGIN") {
				var g = Math.floor(session.createDate/1000/60/60/24);
				var n = gun-g;
				if (n < online.length) {
					online[n] = 1;
				}			
			}
		});
		for (var i=0;i<prop.online_agents_by_day.length;i++) {
			prop.online_agents_by_day[i] += online[i];
		}
		if (!agent.deleted) {
			prop.total_agent++;
		}
		agent.pmap = {};
		
		$.each(agent.properties, function (i, p) {
			var label = p.propertyName;
			var value = p.propertyValue;
			prop.properties[label] = "1";
			agent.pmap[label] = value;
		});
	});
	prop.online_agent = prop.online_agents_by_day[0];
	$("#lta_ahenk_sayisi_gunluk").html("");
	for (var i=0; i<prop.online_agents_by_day.length; i++) {
		var d  = addDays(now, -i);
		$("#lta_ahenk_sayisi_gunluk").prepend("<tr><td>"+lpad(d.getDate(), 2)+"/"+lpad(d.getMonth()+1,2)+"/"+ d.getFullYear()+"</td><td>"+prop.total_agents_by_day[i]+"</td><td>"+prop.online_agents_by_day[i]+"</td></tr>");
	}
	var v = $("#lta_ahenk_prop").val();
	$("#lta_ahenk_prop").html("");
	$("#lta_ahenk_prop").append("<option></option>");
	
	var list = [];
	$.each(prop.properties, function (i, x) {
		list.push(i);
	});
	list.sort();

	$.each(list, function (i, p) {
		var value = list[i];
		var label = value;
		if (label == 'hardware.disk.used' || 
			label == 'sessions.userNames' ||
			label == 'hostname' ||
			label == 'macAddresses' ||
			label == 'hardware.disk.total' ||
			label == 'hardware.disk.free' ||
			label == 'hardware.network.ipAddresses' ||
			label == 'ipAddresses' ||
			label == 'date') {
			return;
		}  
		$("#lta_ahenk_prop").append("<option value='"+value+"'>"+label+"</option>");
	}) ;
	
	
	$("#lta_ahenk_prop").val(v);
	$("#total_agent").html(prop.total_agent);
	$("#offline_agent").html(prop.total_agent-prop.online_agent);
	$("#online_agent").html(prop.online_agent);
	la_prop = prop;
	la_ahenk_prop();
	$('table.highchart').highchartTable();
}

function addDays(date, days) {
    return new Date(
        date.getFullYear(),
        date.getMonth(),
        date.getDate() + days,
        date.getHours(),
        date.getMinutes(),
        date.getSeconds(),
        date.getMilliseconds()
    );
}

function lpad(n, width, z) {
	z = z || '0';
	n = n + '';
	return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
}

function la_ajax_setting() {
	return {
		"async" : true,
		"crossDomain" : true,
		"url" : $("#url").val()+"/agent/list",
		"data": {
			"username": $("#username").val(),
			"password": $("#password").val()
		},
		"method" : "POST"
	}
}

function la_test_connection() {
	var settings = la_ajax_setting();
	settings["success"] = function() {
		la_message("Bağlantı başarılı.","success");
	},
	settings["error"] = function() {
		la_message("Bağlantı hatası.","danger");
	}
	$.ajax(settings).done(function(response) {
		console.log(response);
	});

}

function la_width() {
	$("[width-min]").each(function () {
		$(this).width($(this).width()-parseInt($(this).attr("width-min")));
	});
	$(".h1, .h2, .h3, h1, h2, h3, .page-header").css({"margin-top": "10px", "margin-bottom": "2px"})
}

function la_get_data() {
	if (!$("#url").val()) {
		$(".lta_on_air").hide();
		la_message("Lütfen bağlantı parametrelerini giriniz.")
		return;
	}
	var settings = la_ajax_setting();
	settings["success"] = function(resp) {
		if (resp.status != "OK") {
			$(".lta_on_air").hide();
			la_message("Bağlantı hatası.<br><small>"+resp.messages.join("<br>")+"</small>","danger");
		}
		else {
			$(".lta_on_air").show();
			la_db = resp.resultMap;
			la_refresh_data();
			la_message("Bilgiler yenilendi.","info");
			var r = parseInt($("#refresh_rate").val());
			if (r) {
				if (la_timeout) {
					clearTimeout(la_timeout);
				}
				la_timeout = setTimeout(function () {
					la_get_data();
				}, r*1000);
			}
		}
	},
	settings["error"] = function() {
		$(".lta_on_air").hide();
		la_message("Bağlantı hatası.","danger");
	}
	$.ajax(settings);
}

function la_init_clock() {
	var d = new Date();
	$("#tla_clock").html(d.toLocaleString());
	setInterval(function () {
		var d = new Date();
		$("#tla_clock").html(d.toLocaleString());
	}, 1000);
}

function la_save() {
	localStorage.setItem("la_url", $("#url").val());
	localStorage.setItem("la_username", $("#username").val());
	localStorage.setItem("la_password", $("#password").val());
	localStorage.setItem("la_refresh_rate", $("#refresh_rate").val());
	la_message("Kaydedildi.");
}

function la_load() {
	$("#url").val(localStorage.getItem("la_url"));
	$("#username").val(localStorage.getItem("la_username"));
	$("#password").val(localStorage.getItem("la_password"));
	$("#refresh_rate").val(localStorage.getItem("la_refresh_rate"));
	la_message("Bilgiler okundu.","success");
}

function la_message(s, t) {
	if (!t) {
		t = "info";
	}
	var x = '<div class="alert alert-'+t+'"><a href="#" class="close" data-dismiss="alert" aria-label="close" style="margin-left: 30px;">&times;</a>'+s+'</div>';
	$("#lta_message").html(x);
	setTimeout(function () {
		$("#lta_message").html("");
	}, 1500)
}
