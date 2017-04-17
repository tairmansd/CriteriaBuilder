(function () {

   function ExpressionBuilder(entity, IMPLICIT_CLEAR) {
        this._spIndexes = [];
        this._epIndexes = [];
        this._expressionQueue = [];
        this._entityName = entity;

        if(!entity) {
            throw new Error("Entity not defined as a constructor parameter ExpressionBuilder");
        }

        function validate() {
            // TODO check for syntax errors in string

            var doesStartsWithEP = false;

            if(this._spIndexes.length > 0 && this._epIndexes.length > 0) {
                doesStartsWithEP = (this._epIndexes[0] < this._spIndexes[0]);

                this._validationMessage = doesStartsWithEP ? "Expression starts with end parenthesis.": this._validationMessage;
                if(doesStartsWithEP) return;
            }
            
            var spIsEqualToEp = this._spIndexes.length == this._epIndexes.length;

            this._validationMessage = !spIsEqualToEp ? "Number Start parenthesis doesn't match with End parenthesis." : this._validationMessage;
            if(!spIsEqualToEp) return;

            var hasRedundantExpression = this._expressionQueue.join("").split(new RegExp("AND|OR")).filter(function(obj) {
                return obj == "";
            }).length > 0;

            this._validationMessage = hasRedundantExpression ? "Expression is malformed, no expression is defined between two AND or OR operators." : this._validationMessage;
            if(hasRedundantExpression) return;

            var allExpressionAreValid = true;

            if(!hasRedundantExpression) {
            	var resultantArr = this._expressionQueue.join("").split(new RegExp("AND|OR")).filter(function(obj) {
                    var expression_regex = new RegExp("[(]+?[a-zA-z0-9]+[:!<>][=]?[*]?[a-zA-z0-9-']+[*]?[)]+", "g");
                    return !expression_regex.test(obj);
                });
            	
                allExpressionAreValid = resultantArr.length == 0;

                this._validationMessage = !allExpressionAreValid ? "Found badly formatted expressions" : this._validationMessage;
                if(!allExpressionAreValid) return;
            }

            return !doesStartsWithEP && spIsEqualToEp && !hasRedundantExpression && allExpressionAreValid;
        }

        this.sp = function() {
            var index = this._expressionQueue.length;
            this._expressionQueue[index] = "(";
            this._spIndexes.push(index);
            return this;
        };
        this.ep = function() {
            var index = this._expressionQueue.length;
            this._expressionQueue[index] = ")";
            this._epIndexes.push(index);
            return this;
        };
        this.and = function() {
            this._expressionQueue.push("AND");
            return this;
        };
        this.or = function() {
            this._expressionQueue.push("OR");
            return this;
        };
        this.addExp = function(expression) {
            if(!(typeof expression === 'string')) {
                throw new TypeError("AddExp expected String recieved:"+expression);
            } else {
                this._expressionQueue.push(expression);
            }
            return this;
        };
        this.build = function() {
            if(validate.call(this)) {
                var url = "api/criteria/search/"+this._entityName;
                var queryURI = "query=" + this._expressionQueue.join("");
                var promise = new Promise(function(resolve, reject) {
                    var request = new XMLHttpRequest();
                    request.open('GET', url + "?" + queryURI);
                    request.responseType = 'json';
                    request.onload = function() {
                        if (request.status === 200) {
                        	console.log(request.response);
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
                return promise;
            } else {
                throw new Error("MALFORMED EXPRESSION: "+this._validationMessage);
                this._validationMessage = undefined;
            }
        };
        this.clear = function() {
        	this._spIndexes = [];
            this._epIndexes = [];
            this._expressionQueue = [];
            this._validationMessage = undefined;
        };
     };

    var Expression = {
        "eq": function(key, value) {
            return "(" + key + ":" + value + ")";
        },
        "ne": function(key, value) {
            return "(" + key + "!" + value + ")";
        },
        "lt": function(key, value) {
            return "(" + key + "<" + value + ")";
        },
        "lte": function(key, value) {
            return "(" + key + "<=" + value + ")";
        },
        "gt": function(key, value) {
            return "(" + key + ">" + value + ")";
        },
        "gte": function(key, value) {
            return "(" + key + ">=" + value + ")";
        },
        "like": function(key, value) {
            return "(" + key + ":*" + value + "*)";
        }
    };

    window.ExpressionBuilder = ExpressionBuilder;
    window.Expression        = Expression;
})();