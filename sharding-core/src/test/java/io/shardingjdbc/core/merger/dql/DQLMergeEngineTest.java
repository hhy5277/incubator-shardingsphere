/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.merger.dql;

import com.google.common.base.Optional;
import io.shardingjdbc.core.constant.AggregationType;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.OrderDirection;
import io.shardingjdbc.core.merger.QueryResult;
import io.shardingjdbc.core.merger.MergedResult;
import io.shardingjdbc.core.merger.dql.groupby.GroupByMemoryMergedResult;
import io.shardingjdbc.core.merger.dql.groupby.GroupByStreamMergedResult;
import io.shardingjdbc.core.merger.dql.iterator.IteratorStreamMergedResult;
import io.shardingjdbc.core.merger.dql.orderby.OrderByStreamMergedResult;
import io.shardingjdbc.core.merger.dql.pagination.LimitDecoratorMergedResult;
import io.shardingjdbc.core.merger.fixture.TestQueryResult;
import io.shardingjdbc.core.parsing.parser.context.OrderItem;
import io.shardingjdbc.core.parsing.parser.context.limit.Limit;
import io.shardingjdbc.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DQLMergeEngineTest {
    
    private DQLMergeEngine mergeEngine;
    
    private List<QueryResult> queryResults;
    
    private SelectStatement selectStatement;
    
    @Before
    public void setUp() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject(1)).thenReturn(0);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("count(*)");
        queryResults = Collections.<QueryResult>singletonList(new TestQueryResult(resultSet));
        selectStatement = new SelectStatement();
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResult() throws SQLException {
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        assertThat(mergeEngine.merge(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildIteratorStreamMergedResultWithLimit() throws SQLException {
        selectStatement.setLimit(new Limit(DatabaseType.MySQL));
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(IteratorStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResult() throws SQLException {
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        assertThat(mergeEngine.merge(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildOrderByStreamMergedResultWithLimit() throws SQLException {
        selectStatement.setLimit(new Limit(DatabaseType.MySQL));
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(OrderByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResult() throws SQLException {
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        assertThat(mergeEngine.merge(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByStreamMergedResultWithLimit() throws SQLException {
        selectStatement.setLimit(new Limit(DatabaseType.MySQL));
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        selectStatement.getOrderByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByStreamMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResult() throws SQLException {
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        assertThat(mergeEngine.merge(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithLimit() throws SQLException {
        selectStatement.setLimit(new Limit(DatabaseType.MySQL));
        selectStatement.getGroupByItems().add(new OrderItem(1, OrderDirection.DESC, OrderDirection.ASC));
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnly() throws SQLException {
        selectStatement.getItems().add(new AggregationSelectItem(AggregationType.COUNT, "(*)", Optional.<String>absent()));
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        assertThat(mergeEngine.merge(), instanceOf(GroupByMemoryMergedResult.class));
    }
    
    @Test
    public void assertBuildGroupByMemoryMergedResultWithAggregationOnlyWithLimit() throws SQLException {
        selectStatement.setLimit(new Limit(DatabaseType.MySQL));
        selectStatement.getItems().add(new AggregationSelectItem(AggregationType.COUNT, "(*)", Optional.<String>absent()));
        mergeEngine = new DQLMergeEngine(queryResults, selectStatement);
        MergedResult actual = mergeEngine.merge();
        assertThat(actual, instanceOf(LimitDecoratorMergedResult.class));
        assertThat(((LimitDecoratorMergedResult) actual).getMergedResult(), instanceOf(GroupByMemoryMergedResult.class));
    }
}
