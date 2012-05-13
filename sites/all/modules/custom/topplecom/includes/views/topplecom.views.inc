<?php

/**
 * Alters view defined in commerce_product to work better with topple
 * @param $data
 */
function topplecom_views_data_alter(&$data){

  //exposes commerce products in a way that topple can handle to views
  $data['commerce_product']['table']['group'] = t('Topple Products');
  
  $data['commerce_product']['table']['base'] = array(
    'field' => 'product_id',
    'title' => 'Topple Products',
    'help' => 'Exposes Product information in a way that Topple can use in views',
    'weight' => -100,   
  );
  
  return $data;  
}