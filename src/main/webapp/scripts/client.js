(function() {
	window.addEventListener ? 
			window.addEventListener("load", init, false) : 
			window.attachEvent && window.attachEvent("onload", init);
	
	function init() {
		console.log("Document loaded");

		window.CBFactory = {
		    IMPLICIT_CLEAR: true,
		    FAIL_SAFE_MECHANISM: false,
		    type: undefined,
		    getInstance: function(FACTORY_TYPE, data) {
		        this.type = FACTORY_TYPE;

		        var instance = new CriteriaBuilder(data);

		        if (FACTORY_TYPE.toUpperCase() == 'FRONTEND') {
		            instance.prototype.build = function build() {
		                var a = performance.now();
		                var result = [];
		                for (var i = 0; i < this._list.length; i++) {;
		                    var obj = this._list[i];
		                    var resolvedExpression = eval(expressionResolver(obj, this._expressionObj._expression, this._expressionObj._prop));
		                    if (resolvedExpression) {
		                        result.push(obj);
		                    }
		                }
		                var b = performance.now();
		                console.log(b - a);
		                return new Criteria(result);
		            };

		            return instance;
		        } else if (FACTORY_TYPE.toUpperCase() == 'BACKEND') {

		            instance.prototype.build = function build(url) {
		                return new Promise(function(resolve, reject) {
		                    var request = new XMLHttpRequest();
		                    request.open('GET', url + "?" + this._expressionObj._expression);
		                    request.responseType = 'blob';
		                    request.onload = function() {
		                        if (request.status === 200) {
		                            resolve(request.response);
		                        } else {
		                            reject(Error('Server api error; error code:' + request.statusText));
		                        }
		                    };
		                    request.onerror = function() {
		                        reject(Error('Connection Error'));
		                    };
		                    request.send();
		                });
		            }

		            return instance;
		        } else {
		            throw errorMsg.UNSUPPORTED_FACTORY_TYPE;
		        }
		    }
		};

		var errorMsg = {
		    "UNSUPPORTED_FACTORY_TYPE": "unsupported FACTORY_TYPE. Please select either FRONTEND or BACKEND as factory type.",
		    "UNSUPPORTED_BUILDER_PARAM_TYPE_F": "failed while creating frontend criteria builder. unsupported param type it should be JSON array.",
		    "UNSUPPORTED_BUILDER_PARAM_TYPE_B": "failed while creating backend criteria builder. unsupported param type it should be entity string."
		};

		//NameSpace
		var CriteriaBuilder = function(param) {
		    var error = (window.CBFactory.type == 'FRONTEND' && param.constructor !== [].constructor) ? errorMsg.UNSUPPORTED_BUILDER_PARAM_TYPE_F :
		        (window.CBFactory.type == 'BACKEND' && param.constructor !== "".constructor) ? errorMsg.UNSUPPORTED_BUILDER_PARAM_TYPE_B : null;
		    if (error) {
		        throw error;
		    }

		    this._param = list;
		    this._expressionObj = undefined;
		    this.addExpression = function(expression) {
		        this._expressionObj = expression;
		    };
		};

		var Criteria = function(resultset) {
		    this._resultset = resultset;
		};

		Criteria.prototype.list = function list() {
		    return this._resultset;
		};

		Criteria.prototype.limit = function limit(value) {
		    return this._resultset.splice(0, value);
		};

		Criteria.prototype.sort = function sort(sortObject) {
		    var sort_by = function(field, reverse, primer) {
		        var key = primer ? function(x) {
		            return primer(x[field])
		        } : function(x) {
		            return x[field]
		        };
		        reverse = !reverse ? 1 : -1;
		        return function(a, b) {
		            return a = key(a), b = key(b), reverse * ((a > b) - (b > a));
		        }
		    };
		    return this._resultset.sort(sort_by(sortObject.property, sortObject.order, function(a) {
		        return a.toUpperCase()
		    }));
		};

		Criteria.prototype.projection = function projection(properties) {
		    for (var object in this._resultset) {
		        for (var property in properties) {
		            if (this._resultset.hasOwnProperty(property)) {
		                delete object[property];
		            }
		        }
		    }
		};

		var expressionResolver = function(obj, exp, props) {
		    var expression = exp;
		    for (var i = 0; i < props.length; i++) {
		        var property = props[i];
		        var value = getValue(obj, property);
		        expression.replaceAll("obj." + property, value);
		    }
		    return expression;
		};

		var getValue = function getValue(object, property) {
		    var properties = property.split(".");
		    var temp = Object.clone(object);
		    while (properties.length > 0) {
		        temp = temp[properties[0]];
		        properties.splice(0, 1);
		    }
		    return temp;
		};

		var valueResolver = function(value) {
		    var supportedTypes = ["Date", "Number", "String", "Boolean", "Array", "Object"];
		    var result = undefined;
		    try {
		        var index = supportedTypes.indexOf(value.constructor.name);
		        if (index != -1) {
		            switch (supportedTypes[index]) {
		                case "Date":
		                    result = value.getTime().toString();
		                    break;
		                case "Object":
		                    result = JSON.stringify(value);
		                    break;
		                default:
		                    result = value.toString();
		            }
		        } else {
		            throw "UNSUPPORTED_DATA_TYPE";
		        }
		    } catch (e) {
		        console.error(e, "unspported data type for value:" + value);
		    }
		    return "'" + result + "'";
		}

		var ExpressionBuilder = function() {
		    this._expression = "";
		    this._prop = [];
		    this._operatorHandler = {
		        "=": function(op, va) {
		            return "obj." + op + "==" + valueResolver(va);
		        },
		        ":": function(op, va) {
		            return "obj." + op + "===" + valueResolver(va);
		        },
		        "<": function(op, va) {
		            return "obj." + op + "<" + valueResolver(va);
		        },
		        "<=": function(op, va) {
		            return "obj." + op + "<=" + valueResolver(va);
		        },
		        ">": function(op, va) {
		            return "obj." + op + ">" + valueResolver(va);
		        },
		        ">=": function(op, va) {
		            return "obj." + op + ">=" + valueResolver(va);
		        },
		        "IN": function(ar, va) {
		            //works both for array and String contains
		            return "obj." + ar + ".indexOf('" + va + "') != -1";
		        }
		    };
		};

		//expression
		ExpressionBuilder.prototype.ex = function(operand, operator, value) {
		    if (this._prop.indexOf(operand) == -1) {
		        this._prop.push(operand);
		    }
		    this._expression = this._expression + this._operatorHandler[operator](operand, value);
		    return this;
		};

		ExpressionBuilder.prototype.and = function() {
		    this._expression = this._expression + "&&";
		    return this;
		};

		ExpressionBuilder.prototype.or = function() {
		    this._expression = this._expression + "||";
		    return this;
		};

		//open paranthesis
		ExpressionBuilder.prototype.op = function() {
		    this._expression = this._expression + "(";
		    return this;
		};

		//end paranthesis
		ExpressionBuilder.prototype.ep = function() {
		    this._expression = this._expression + ")";
		    return this;
		};

		ExpressionBuilder.prototype.build = function() {
		    var json = JSON.parse(JSON.stringify(this));
		    delete json._operatorHandler;
		    return json;
		};

		window.ExpressionBuilder = ExpressionBuilder;
		window.CriteriaBuilder = CriteriaBuilder
	};
})();