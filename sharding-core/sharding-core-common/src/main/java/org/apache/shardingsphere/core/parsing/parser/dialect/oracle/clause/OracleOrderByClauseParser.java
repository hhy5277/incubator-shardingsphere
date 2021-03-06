/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parsing.parser.dialect.oracle.clause;

import org.apache.shardingsphere.core.constant.OrderDirection;
import org.apache.shardingsphere.core.parsing.lexer.LexerEngine;
import org.apache.shardingsphere.core.parsing.lexer.dialect.oracle.OracleKeyword;
import org.apache.shardingsphere.core.parsing.parser.clause.OrderByClauseParser;
import org.apache.shardingsphere.core.parsing.parser.exception.SQLParsingException;

/**
 * Order by clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleOrderByClauseParser extends OrderByClauseParser {
    
    public OracleOrderByClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected OrderDirection getNullOrderDirection() {
        if (!getLexerEngine().skipIfEqual(OracleKeyword.NULLS)) {
            return OrderDirection.ASC;
        }
        if (getLexerEngine().skipIfEqual(OracleKeyword.FIRST)) {
            return OrderDirection.ASC;
        }
        if (getLexerEngine().skipIfEqual(OracleKeyword.LAST)) {
            return OrderDirection.DESC;
        }
        throw new SQLParsingException(getLexerEngine());
    }
}
