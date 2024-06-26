package org.cboard.jdbc;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.cboard.cache.CacheManager;
import org.cboard.cache.HeapCacheManager;
import org.cboard.dataprovider.AggregateProvider;
import org.cboard.dataprovider.DataProvider;
import org.cboard.dataprovider.annotation.DatasourceParameter;
import org.cboard.dataprovider.annotation.ProviderName;
import org.cboard.dataprovider.annotation.QueryParameter;
import org.cboard.dataprovider.config.AggConfig;
import org.cboard.dataprovider.config.DimensionConfig;
import org.cboard.dataprovider.config.ValueConfig;
import org.cboard.dataprovider.result.AggregateResult;
import org.cboard.dataprovider.result.ColumnIndex;
import org.cboard.exception.CBoardException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by yfyuan on 2016/8/17.
 */
@ProviderName(name = "jdbc")
public class JdbcDataProvider extends DataProvider implements AggregateProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcDataProvider.class);

    @Value("${dataprovider.resultLimit:200000}")
    private int resultLimit;

    @DatasourceParameter(label = "{{'DATAPROVIDER.JDBC.DRIVER'|translate}}", type = DatasourceParameter.Type.Input, order = 1)
    private String DRIVER = "driver";

    @DatasourceParameter(label = "{{'DATAPROVIDER.JDBC.JDBCURL'|translate}}", type = DatasourceParameter.Type.Input, order = 2)
    private String JDBC_URL = "jdbcurl";

    @DatasourceParameter(label = "{{'DATAPROVIDER.JDBC.USERNAME'|translate}}", type = DatasourceParameter.Type.Input, order = 3)
    private String USERNAME = "username";

    @DatasourceParameter(label = "{{'DATAPROVIDER.JDBC.PASSWORD'|translate}}", type = DatasourceParameter.Type.Password, order = 4)
    private String PASSWORD = "password";

    @DatasourceParameter(label = "{{'DATAPROVIDER.JDBC.POOLEDCONNECTION'|translate}}", type = DatasourceParameter.Type.Checkbox, order = 5)
    private String POOLED = "pooled";

    @QueryParameter(label = "{{'DATAPROVIDER.JDBC.SQLTEXT'|translate}}", type = QueryParameter.Type.TextArea, order = 1)
    private String SQL = "sql";

    private static final CacheManager<Map<String, Integer>> typeCahce = new HeapCacheManager<>();

    private static final ConcurrentMap<String, DataSource> datasourceMap = new ConcurrentHashMap<>();

    private String getKey(Map<String, String> dataSource, Map<String, String> query) {
        return Hashing.md5().newHasher().putString(JSONObject.toJSON(dataSource).toString() + JSONObject.toJSON(query).toString(), Charsets.UTF_8).hash().toString();
    }

    public String[][] getData(Map<String, String> dataSource, Map<String, String> query) throws Exception {

        LOG.debug("Execute JdbcDataProvider.getData() Start!");
        String sql = query.get(SQL);
        List<String[]> list = null;
        LOG.info("SQL String: " + sql);

        try (Connection con = getConnection(dataSource)) {
            Statement ps = con.createStatement();
            ResultSet rs = ps.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            list = new LinkedList<>();
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = metaData.getColumnLabel(i + 1);
            }
            list.add(row);
            int resultCount = 0;
            while (rs.next()) {
                resultCount++;
                if (resultCount > resultLimit) {
                    throw new CBoardException("Cube result count is greater than limit " + resultLimit);
                }
                row = new String[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    row[j] = rs.getString(j + 1);
                }
                list.add(row);
            }
        } catch (Exception e) {
            LOG.error("ERROR:" + e.getMessage());
            throw new Exception("ERROR:" + e.getMessage(), e);
        }

        return list.toArray(new String[][]{});
    }

    /**
     * Convert the sql text to subquery string:
     * remove blank line
     * remove end semicolon ;
     *
     * @param rawQueryText
     * @return
     */
    private String getAsSubQuery(String rawQueryText) {
        String deletedBlankLine = rawQueryText.replaceAll("(?m)^[\\s\t]*\r?\n", "").trim();
        return deletedBlankLine.endsWith(";") ? deletedBlankLine.substring(0, deletedBlankLine.length() - 1) : deletedBlankLine;
    }

    private Connection getConnection(Map<String, String> dataSource) throws Exception {
        String v = dataSource.get(POOLED);
        if (v != null && "true".equals(v)) {
            String key = Hashing.md5().newHasher().putString(JSONObject.toJSON(dataSource).toString(), Charsets.UTF_8).hash().toString();
            DataSource ds = datasourceMap.get(key);
            if (ds == null) {
                synchronized (key.intern()) {
                    ds = datasourceMap.get(key);
                    if (ds == null) {
                        Map<String, String> conf = new HashedMap();
                        conf.put(DruidDataSourceFactory.PROP_URL, dataSource.get(JDBC_URL));
                        conf.put(DruidDataSourceFactory.PROP_USERNAME, dataSource.get(USERNAME));
                        conf.put(DruidDataSourceFactory.PROP_PASSWORD, dataSource.get(PASSWORD));
                        conf.put(DruidDataSourceFactory.PROP_INITIALSIZE, "3");
                        ds = DruidDataSourceFactory.createDataSource(conf);
                        datasourceMap.put(key, ds);
                    }
                }
            }
            return ds.getConnection();
        } else {
            String driver = dataSource.get(DRIVER);
            String jdbcurl = dataSource.get(JDBC_URL);
            String username = dataSource.get(USERNAME);
            String password = dataSource.get(PASSWORD);
            Class.forName(driver);
            Properties props = new Properties();
            props.setProperty("user", username);
            props.setProperty("password", password);
            return DriverManager.getConnection(jdbcurl, props);
        }
    }

    @Override
    public String[][] queryDimVals(Map<String, String> dataSource, Map<String, String> query, String columnName, AggConfig config) throws Exception {
        String fsql = null;
        String exec = null;
        String sql = getAsSubQuery(query.get(SQL));
        List<String> filtered = new ArrayList<>();
        List<String> nofilter = new ArrayList<>();
        if (config != null) {
            Stream<DimensionConfig> c = config.getColumns().stream();
            Stream<DimensionConfig> r = config.getRows().stream();
            Stream<DimensionConfig> f = config.getFilters().stream();
            Stream<DimensionConfig> filters = Stream.concat(Stream.concat(c, r), f);
            Map<String, Integer> types = getColumnType(dataSource, query);
            Stream<DimensionConfigHelper> filterHelpers = filters.map(fe -> new DimensionConfigHelper(fe, types.get(fe.getColumnName())));
            String whereStr = assembleSqlFilter(filterHelpers, "WHERE");
            fsql = "SELECT __view__.%s FROM (%s) __view__ %s GROUP BY __view__.%s";
            exec = String.format(fsql, columnName, sql, whereStr, columnName);
            LOG.info(exec);
            try (Connection connection = getConnection(dataSource);
                 Statement stat = connection.createStatement();
                 ResultSet rs = stat.executeQuery(exec)) {
                while (rs.next()) {
                    filtered.add(rs.getString(1));
                }
            } catch (Exception e) {
                LOG.error("ERROR:" + e.getMessage());
                throw new Exception("ERROR:" + e.getMessage(), e);
            }
        }
        fsql = "SELECT __view__.%s FROM (%s) __view__ GROUP BY __view__.%s";
        exec = String.format(fsql, columnName, sql, columnName);
        LOG.info(exec);
        try (Connection connection = getConnection(dataSource);
             Statement stat = connection.createStatement();
             ResultSet rs = stat.executeQuery(exec)) {
            while (rs.next()) {
                nofilter.add(rs.getString(1));
            }
        } catch (Exception e) {
            LOG.error("ERROR:" + e.getMessage());
            throw new Exception("ERROR:" + e.getMessage(), e);
        }
        return new String[][]{config == null ? nofilter.toArray(new String[]{}) : filtered.toArray(new String[]{}), nofilter.toArray(new String[]{})};
    }

    /**
     * Parser a single filter configuration to sql syntax
     */
    private Function<DimensionConfigHelper, String> filter2SqlCondtion = (config) -> {
        if (config.getValues().size() == 0) {
            return null;
        }
        switch (config.getFilterType()) {
            case "=":
            case "eq":
                return config.getColumnName() + " IN (" + IntStream.range(0, config.getValues().size()).boxed().map(i -> config.getValueStr(i)).collect(Collectors.joining(",")) + ")";
            case "≠":
            case "ne":
                return config.getColumnName() + " NOT IN (" + IntStream.range(0, config.getValues().size()).boxed().map(i -> config.getValueStr(i)).collect(Collectors.joining(",")) + ")";
            case ">":
                return config.getColumnName() + " > " + config.getValueStr(0);
            case "<":
                return config.getColumnName() + " < " + config.getValueStr(0);
            case "≥":
                return config.getColumnName() + " >= " + config.getValueStr(0);
            case "≤":
                return config.getColumnName() + " <= " + config.getValueStr(0);
            case "(a,b]":
                if (config.getValues().size() >= 2) {
                    return "(" + config.getColumnName() + " > '" + config.getValueStr(0) + "' AND " + config.getColumnName() + " <= " + config.getValueStr(1) + ")";
                } else {
                    return null;
                }
            case "[a,b)":
                if (config.getValues().size() >= 2) {
                    return "(" + config.getColumnName() + " >= " + config.getValueStr(0) + " AND " + config.getColumnName() + " < " + config.getValueStr(1) + ")";
                } else {
                    return null;
                }
            case "(a,b)":
                if (config.getValues().size() >= 2) {
                    return "(" + config.getColumnName() + " > " + config.getValueStr(0) + " AND " + config.getColumnName() + " < " + config.getValueStr(1) + ")";
                } else {
                    return null;
                }
            case "[a,b]":
                if (config.getValues().size() >= 2) {
                    return "(" + config.getColumnName() + " >= " + config.getValueStr(0) + " AND " + config.getColumnName() + " <= " + config.getValueStr(1) + ")";
                } else {
                    return null;
                }
        }
        return null;
    };

    /**
     * Assemble all the filter to a legal sal where script
     *
     * @param filterStream
     * @param prefix       HAVING or WHERE
     * @return
     */
    private String assembleSqlFilter(Stream<DimensionConfigHelper> filterStream, String prefix) {
        StringJoiner where = new StringJoiner(" AND ", prefix + " ", "");
        where.setEmptyValue("");
        filterStream.map(filter2SqlCondtion).filter(e -> e != null).forEach(where::add);
        return where.toString();
    }

    private String assembleAggValColumns(Stream<ValueConfig> selectStream) {
        StringJoiner columns = new StringJoiner(", ", "", " ");
        columns.setEmptyValue("");
        selectStream.map(toSelect).filter(e -> e != null).forEach(columns::add);
        return columns.toString();
    }

    private String assembleDimColumns(Stream<DimensionConfig> columnsStream) {
        StringJoiner columns = new StringJoiner(", ", "", " ");
        columns.setEmptyValue("");
        columnsStream.map(g -> g.getColumnName()).distinct().filter(e -> e != null).forEach(columns::add);
        return columns.toString();
    }

    private ResultSetMetaData getMetaData(String subQuerySql, Statement stat) throws Exception {
        ResultSetMetaData metaData;
        try {
            stat.setMaxRows(100);
            String fsql = "\nSELECT * FROM (\n%s\n) __view__ WHERE 1=0";
            String sql = String.format(fsql, subQuerySql);
            LOG.info(sql);
            ResultSet rs = stat.executeQuery(sql);
            metaData = rs.getMetaData();
        } catch (Exception e) {
            LOG.error("ERROR:" + e.getMessage());
            throw new Exception("ERROR:" + e.getMessage(), e);
        }
        return metaData;
    }

    private Map<String, Integer> getColumnType(Map<String, String> dataSource, Map<String, String> query) throws Exception {
        Map<String, Integer> result = null;
        String key = getKey(dataSource, query);
        String subQuerySql = getAsSubQuery(query.get(SQL));
        result = typeCahce.get(key);
        if (result != null) {
            return result;
        } else {
            try (
                    Connection connection = getConnection(dataSource);
                    Statement stat = connection.createStatement()
            ) {
                ResultSetMetaData metaData = getMetaData(subQuerySql, stat);
                int columnCount = metaData.getColumnCount();
                result = new HashedMap();
                for (int i = 0; i < columnCount; i++) {
                    result.put(metaData.getColumnLabel(i + 1), metaData.getColumnType(i + 1));
                }
                typeCahce.put(key, result, 12 * 60 * 60 * 1000);
                return result;
            }
        }
    }

    @Override
    public String[] getColumn(Map<String, String> dataSource, Map<String, String> query) throws Exception {
        String subQuerySql = getAsSubQuery(query.get(SQL));
        try (
                Connection connection = getConnection(dataSource);
                Statement stat = connection.createStatement()
        ) {
            ResultSetMetaData metaData = getMetaData(subQuerySql, stat);
            int columnCount = metaData.getColumnCount();
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = metaData.getColumnLabel(i + 1);
            }
            return row;
        }
    }

    @Override
    public AggregateResult queryAggData(Map<String, String> dataSource, Map<String, String> query, AggConfig config) throws Exception {
        String exec = getQueryAggDataSql(dataSource, query, config);
        List<String[]> list = new LinkedList<>();
        LOG.info(exec);
        try (
                Connection connection = getConnection(dataSource);
                Statement stat = connection.createStatement();
                ResultSet rs = stat.executeQuery(exec)
        ) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int j = 0; j < columnCount; j++) {
                    row[j] = rs.getString(j + 1);
                }
                list.add(row);
            }
        } catch (Exception e) {
            LOG.error("ERROR:" + e.getMessage());
            throw new Exception("ERROR:" + e.getMessage(), e);
        }

        // recreate a dimension stream
        Stream<DimensionConfig> dimStream = Stream.concat(config.getColumns().stream(), config.getRows().stream());
        List<ColumnIndex> dimensionList = dimStream.map(ColumnIndex::fromDimensionConfig).collect(Collectors.toList());
        dimensionList.addAll(config.getValues().stream().map(ColumnIndex::fromValueConfig).collect(Collectors.toList()));
        IntStream.range(0, dimensionList.size()).forEach(j -> dimensionList.get(j).setIndex(j));
        String[][] result = list.toArray(new String[][]{});
        return new AggregateResult(dimensionList, result);
    }

    private String getQueryAggDataSql(Map<String, String> dataSource, Map<String, String> query, AggConfig config) throws Exception {
        Stream<DimensionConfig> c = config.getColumns().stream();
        Stream<DimensionConfig> r = config.getRows().stream();
        Stream<DimensionConfig> f = config.getFilters().stream();
        Stream<DimensionConfig> filters = Stream.concat(Stream.concat(c, r), f);
        Map<String, Integer> types = getColumnType(dataSource, query);
        Stream<DimensionConfigHelper> predicates = filters.map(fe -> new DimensionConfigHelper(fe, types.get(fe.getColumnName())));
        Stream<DimensionConfig> dimStream = Stream.concat(config.getColumns().stream(), config.getRows().stream());

        String dimColsStr = assembleDimColumns(dimStream);
        String aggColsStr = assembleAggValColumns(config.getValues().stream());
        String whereStr = assembleSqlFilter(predicates, "WHERE");
        String groupByStr = StringUtils.isBlank(dimColsStr) ? "" : "GROUP BY " + dimColsStr;

        StringJoiner selectColsStr = new StringJoiner(",");
        if (!StringUtils.isBlank(dimColsStr)) {
            selectColsStr.add(dimColsStr);
        }
        if (!StringUtils.isBlank(aggColsStr)) {
            selectColsStr.add(aggColsStr);
        }

        String subQuerySql = getAsSubQuery(query.get(SQL));
        String fsql = "\nSELECT %s FROM (\n%s\n) __view__ %s %s";
        String exec = String.format(fsql, selectColsStr, subQuerySql, whereStr, groupByStr);
        return exec;
    }

    @Override
    public String viewAggDataQuery(Map<String, String> dataSource, Map<String, String> query, AggConfig config) throws Exception {
        return getQueryAggDataSql(dataSource, query, config);
    }

    private Function<ValueConfig, String> toSelect = (config) -> {
        switch (config.getAggType()) {
            case "sum":
                return "SUM(__view__." + config.getColumn() + ") sum" + config.getColumn();
            case "avg":
                return "AVG(__view__." + config.getColumn() + ") avg" + config.getColumn();
            case "max":
                return "MAX(__view__." + config.getColumn() + ") max" + config.getColumn();
            case "min":
                return "MIN(__view__." + config.getColumn() + ") min" + config.getColumn();
            default:
                return "COUNT(__view__." + config.getColumn() + ") count" + config.getColumn();
        }
    };

    private class DimensionConfigHelper extends DimensionConfig {
        private DimensionConfig config;
        private int type;

        public DimensionConfigHelper(DimensionConfig config, int type) {
            this.config = config;
            this.type = type;
        }

        public String getValueStr(int index) {
            switch (type) {
                case Types.VARCHAR:
                case Types.CHAR:
                case Types.NVARCHAR:
                case Types.NCHAR:
                case Types.CLOB:
                case Types.NCLOB:
                case Types.LONGVARCHAR:
                case Types.LONGNVARCHAR:
                case Types.DATE:
                case Types.TIMESTAMP:
                case Types.TIMESTAMP_WITH_TIMEZONE:
                    return "'" + getValues().get(index) + "'";
                default:
                    return getValues().get(index);
            }
        }

        @Override
        public String getColumnName() {
            return config.getColumnName();
        }

        @Override
        public void setColumnName(String columnName) {
            config.setColumnName(columnName);
        }

        @Override
        public String getFilterType() {
            return config.getFilterType();
        }

        @Override
        public void setFilterType(String filterType) {
            config.setFilterType(filterType);
        }

        @Override
        public List<String> getValues() {
            return config.getValues();
        }

        @Override
        public void setValues(List<String> values) {
            config.setValues(values);
        }

    }
}
