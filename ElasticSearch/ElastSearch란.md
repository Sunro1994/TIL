# ElasticSearch

ElasticSearch는 많은 데이터를 다루는 검색 엔진으로 고려할 만한 오픈소스 도구이다. ElasticSearch는 ELK 스택(ElasticSearch, Logstash, Kibana, 그리고 Beats)을 구성하는 주요 요소 중 하나로, 다양한 데이터 분석과 검색 작업에서 활용된다.

ElasticSearch와 비교할 수 있는 다른 검색 및 데이터 관리 솔루션으로는 Solr, Splunk, MongoDB, 그리고 Cassandra 등이 있다. 이들 솔루션과 비교했을 때 ElasticSearch는 다음과 같은 강점을 가진다:

1. **빠른 실시간 검색**: ElasticSearch는 검색 속도가 매우 빠르며 실시간 데이터 처리가 가능하다. 이는 Solr와 비슷한 검색 기능을 제공하지만, ElasticSearch는 분산 환경에서 더 높은 성능을 발휘한다.
2. **유연한 데이터 모델**: JSON 기반의 데이터 저장 구조를 사용해 다양한 데이터 유형을 지원한다. 이는 MongoDB의 유연성에 비견될 수 있지만, ElasticSearch는 검색 기능에 더욱 특화되어 있다.
3. **확장성 및 분산 처리**: ElasticSearch는 분산 처리 환경에 최적화되어 있으며, 노드 추가를 통해 수평적 확장이 가능하다. 이는 Cassandra와 유사하지만, ElasticSearch는 검색과 분석 기능에서도 두각을 나타낸다.
4. **통합된 로그 관리**: Splunk가 로그 관리에 특화되어 있다면, ElasticSearch는 Beats와 Logstash를 통해 유사한 기능을 더 저렴한 비용으로 제공한다.

특히 블리자드와 같은 기업에서는 ElasticSearch를 통해 유저 활동을 실시간으로 모니터링하며, 이를 통해 데이터 기반 의사결정을 강화하고 있다. 다만, ElasticSearch는 데이터 구조 설계 및 설정 과정이 상대적으로 복잡할 수 있으며, 대규모 데이터를 처리할 때 클러스터 관리에 추가적인 노력이 필요할 수 있다.

## ElasticSearch와 이커머스 서비스

쿠팡과 같은 이커머스 서비스를 예로 들면, 일반적으로 클라이언트-서버-데이터베이스(DB)로 구성된다. 이 경우 데이터 검색 요청이 많아질수록 많은 테이블을 생성하고 조회하는 과정에서 성능 부하가 발생할 수 있다. 데이터의 양이 늘어나면서 전통적인 관계형 데이터베이스는 점점 더 많은 리소스를 요구하며, 복잡한 쿼리와 조인 작업은 응답 속도를 저하시킬 수 있다.

ElasticSearch를 도입하면 클라이언트-서버-ElasticSearch 서버로 시스템을 구성하여 이러한 성능 문제를 완화할 수 있다. ElasticSearch는 데이터를 효율적으로 인덱싱하여 검색 속도를 크게 향상시키고, 분산 구조를 통해 대량의 데이터를 처리할 때도 확장성을 유지한다. 또한 관계형 데이터베이스와 달리, 복잡한 스키마 설계 없이 JSON 형식으로 데이터를 저장하므로 개발 과정이 단순해질 수 있다.

ElasticSearch의 도입은 다음과 같은 장점을 가진다:

1. **빠른 검색 속도**: 대규모 데이터를 효율적으로 인덱싱하고 검색할 수 있다.
2. **확장성**: 데이터 양이 늘어나도 노드를 추가해 성능 저하를 최소화할 수 있다.
3. **시각화 도구**: Kibana를 사용해 데이터를 시각적으로 분석하고 모니터링할 수 있다.
4. **로그 관리**: Beats와 Logstash를 통해 로그를 수집, 분석, 저장하는 작업이 간단해진다.

## ElasticSearch의 기본 구조

ElasticSearch는 기존 데이터베이스와는 다른 독특한 구조를 가지고 있다. 초보 개발자들도 쉽게 이해할 수 있도록 각 개념을 아래와 같이 설명한다:

- **노드(Node)**: ElasticSearch에서 데이터를 저장하거나 처리하는 기본 단위이다. 하나의 노드는 ElasticSearch 서버를 실행하는 하나의 인스턴스라고 생각하면 된다. 노드는 고유한 이름과 ID를 가지며, 마치 팀 프로젝트에서 하나의 팀원이 특정 역할을 맡는 것처럼 각각의 노드도 역할을 수행한다.

- **클러스터(Cluster)**: 여러 노드가 모여서 하나의 클러스터를 형성한다. 클러스터는 ElasticSearch 전체 시스템을 대표하는 이름이며, 데이터를 분산 저장하고 검색 작업을 수행한다. 클러스터를 하나의 팀이라고 생각한다면, 각각의 노드는 팀원이라고 할 수 있다. 모든 노드가 협력하여 데이터를 처리한다.

- **샤드(Shard)**: 데이터를 실제로 저장하는 작은 조각들이다. 하나의 인덱스가 여러 개의 샤드로 나뉘며, 각 샤드는 특정 데이터를 저장한다. 데이터를 샤드로 나누면 한 노드에 너무 많은 데이터를 저장하지 않아도 되어 시스템 성능이 향상된다. 예를 들어, 책 한 권을 여러 권의 얇은 책으로 나누어 관리한다고 생각할 수 있다.

ElasticSearch의 클러스터는 분산 처리 환경에서 데이터를 효율적으로 관리하도록 설계되었다. 각 노드는 물리적으로 다른 컴퓨터에 위치할 수 있으며, 이를 통해 확장성과 가용성을 극대화할 수 있다. 즉, 시스템에 문제가 생기더라도 다른 노드가 데이터를 백업하거나 작업을 이어받아 안정성을 유지한다.

### 샤딩(Sharding)의 필요성

샤딩은 데이터를 분산해 저장하기 위해 사용된다. 예를 들어, 30만 개의 데이터를 저장해야 하는데 하나의 노드 용량이 15만 개라면 두 개의 노드에 데이터를 분산 저장해야 한다. 이렇게 데이터를 나누어 저장하면 노드 하나에 문제가 생겨도 다른 노드에 데이터가 복제되어 있어 데이터 손실을 방지할 수 있다. 이는 마치 중요한 문서를 여러 곳에 복사본으로 보관하는 것과 비슷하다.

또한, 샤딩은 성능 향상에도 기여한다. 데이터가 여러 샤드로 나뉘어 저장되면 검색 요청을 병렬로 처리할 수 있어 응답 속도가 빨라진다. 예를 들어, 10개의 샤드에 데이터를 저장한 경우, 각 샤드가 동시에 일부 데이터를 검색하므로 전체 검색 시간이 단축된다. 이러한 구조 덕분에 ElasticSearch는 대규모 데이터를 다룰 때도 높은 성능을 유지할 수 있다.

## 데이터 저장 형식

ElasticSearch에서 데이터는 JSON 형식으로 저장된다. 데이터는 고유한 ID를 가지며, 다음과 같은 형태로 표현된다:

```json
{
  "name": "이선로",
  "age": 999
}
```

이 데이터는 논리적으로 인덱스에 저장되며, 물리적으로는 샤드에 저장된다. 여러 개의 샤드는 하나의 인덱스를 구성하며, 이를 통해 데이터의 저장과 검색이 이루어진다.

## ## ElasticSearch CRUD 및 검색 기능

ElasticSearch는 CRUD(Create, Read, Update, Delete) 작업뿐만 아니라 강력한 검색 기능을 제공한다. RESTful API를 사용해 데이터를 관리하고 검색할 수 있으며, 복잡한 쿼리도 간단하게 작성할 수 있다. 아래는 각각의 CRUD 작업에 대한 예시와 결과를 설명한다.

### 1. Create (데이터 생성)

인덱스에 새 데이터를 추가하려면 다음과 같은 요청을 보낸다:

```json
PUT /index_name/_doc/1
{
  "name": "홍길동",
  "age": 30,
  "occupation": "개발자"
}
```

인덱스가 겹칠 수 있는 상황에서는 `_create` 메서드를 사용할 수 있다. 이 메서드는 인덱스가 이미 존재할 경우 새로운 데이터를 추가하지 않고 오류를 반환한다. 예시는 다음과 같다:

```json
PUT /index_name/_create/2
{
  "name": "김철수",
  "age": 25,
  "occupation": "디자이너"
}
```

**결과:**

```json
{
  "_index": "index_name",
  "_type": "_doc",
  "_id": "2",
  "_version": 1,
  "result": "created",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  }
}
```

만약 `2`라는 ID가 이미 존재한다면 다음과 같은 오류가 반환된다:

**오류:**

```json
{
  "error": {
    "type": "version_conflict_engine_exception",
    "reason": "[index_name][_id][2]: version conflict, document already exists",
    "index_uuid": "abc123",
    "shard": "shard_id",
    "index": "index_name"
  },
  "status": 409
}
```

### 2. Read (데이터 조회)

특정 ID에 해당하는 문서를 조회하려면 다음 요청을 보낸다:

```json
GET /index_name/_doc/1
```

**결과:**

```json
{
  "_index": "index_name",
  "_type": "_doc",
  "_id": "1",
  "_version": 1,
  "_source": {
    "name": "홍길동",
    "age": 30,
    "occupation": "개발자"
  }
}
```

### 3. Update (데이터 수정)

기존 문서를 수정하려면 다음 요청을 보낸다:

```json
POST /index_name/_update/1
{
  "doc": {
    "age": 31
  }
}
```

**결과:**

```json
{
  "_index": "index_name",
  "_type": "_doc",
  "_id": "1",
  "_version": 2,
  "result": "updated",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  }
}
```

### 4. Delete (데이터 삭제)

특정 문서를 삭제하려면 다음 요청을 보낸다:

```json
DELETE /index_name/_doc/1
```

**결과:**

```json
{
  "_index": "index_name",
  "_type": "_doc",
  "_id": "1",
  "_version": 3,
  "result": "deleted",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  }
}
```

### 5. Search (데이터 검색)

특정 조건에 맞는 데이터를 검색하려면 다음과 같은 쿼리를 작성한다:

```json
GET /index_name/_search
{
  "query": {
    "match": {
      "name": "홍길동"
    }
  }
}
```

**결과:**

```json
{
  "took": 5,
  "timed_out": false,
  "_shards": {
    "total": 1,
    "successful": 1,
    "skipped": 0,
    "failed": 0
  },
  "hits": {
    "total": {
      "value": 1,
      "relation": "eq"
    },
    "max_score": 1.0,
    "hits": [
      {
        "_index": "index_name",
        "_type": "_doc",
        "_id": "1",
        "_score": 1.0,
        "_source": {
          "name": "홍길동",
          "age": 31,
          "occupation": "개발자"
        }
      }
    ]
  }
}
```

