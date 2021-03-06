package edu.oregonstate.mist.subjectsapi.dao

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.jsonapi.ResourceObject
import edu.oregonstate.mist.subjectsapi.core.Attributes
import org.apache.http.HttpEntity
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.util.EntityUtils

class SubjectsDAO {
    private UtilHttp utilHttp
    private HttpClient httpClient
    private ObjectMapper mapper = new ObjectMapper()

    SubjectsDAO(UtilHttp utilHttp, HttpClient httpClient) {
        this.httpClient = httpClient
        this.utilHttp = utilHttp
    }

    /**
     * Fetches course subjects and returns results in jsonapi format
     *
     * @return
     */
    public def getData() {
        CloseableHttpResponse response
        def data = []
        def sourcePagination = [:]

        try {
            def query = [:]
            response = utilHttp.sendGet(query, httpClient)

            HttpEntity entity = response.getEntity()
            def entityString = EntityUtils.toString(entity)

            data = this.mapper.readValue(entityString,
                    new TypeReference<List<HashMap>>() {
                    })

            EntityUtils.consume(entity)
        } finally {
            response?.close()
        }

        [data: getFormattedData(data), sourcePagination: sourcePagination]
    }

    /**
     * Takes the data from the backend and formats it based on the swagger spec.
     *
     * @param data
     * @return
     */
    private static List<ResourceObject> getFormattedData(def data) {
        List<ResourceObject> result = new ArrayList<ResourceObject>()

        data.each {
            Attributes attributes = new Attributes(
                    abbreviation:   it.abbreviation,
                    title:          it.title
            )

            result << new ResourceObject(id: it.abbreviation, type: 'subjects',
                    attributes: attributes)
        }

        result
    }
}